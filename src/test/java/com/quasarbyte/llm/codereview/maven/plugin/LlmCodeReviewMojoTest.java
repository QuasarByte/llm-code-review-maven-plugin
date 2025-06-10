package com.quasarbyte.llm.codereview.maven.plugin;

import com.quasarbyte.llm.codereview.maven.plugin.model.*;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LlmCodeReviewMojoTest {

    private static final Logger logger = LoggerFactory.getLogger(LlmCodeReviewMojoTest.class);

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start(); // uses a random available port
    }

    @AfterEach
    void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Test
    void testExecuteWithLmStudioMocks() throws IOException {
        // --- MOCK: POST /v1/chat/completions ---
        String chatCompletionResponse = loadTestResource("src/test/resources/com/quasarbyte/llm/codereview/maven/plugin/examples/chatCompletionResponse.json");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(chatCompletionResponse)
        );

        // --- Mojo setup ---
        LlmCodeReviewMojo mojo = new LlmCodeReviewMojo();

        // --- LLM CHAT COMPLETION CONFIGURATION ---
        LlmChatCompletionConfiguration llmChatCompletionConfiguration = new LlmChatCompletionConfiguration()
                .setModel("qwen3-8b");

        // --- REVIEW PARAMETER ---
        PReviewParameter reviewParameter = new PReviewParameter();
        reviewParameter.setReviewName("Find critical defects in code");
        reviewParameter.setSystemPrompts(Collections.singletonList("You are code review assistant."));
        reviewParameter.setReviewPrompts(
                        Arrays.asList(
                                "Please review all these Java files",
                                "Comment style: Human like, friendly, natural, and professional tone; ideal for PRs, reports, and communication."
                        )
                )
                .setLlmChatCompletionConfiguration(llmChatCompletionConfiguration);

        // --- RULE ---
        PRule rule = new PRule();
        rule.setCode("001");
        rule.setDescription("Find possible java.lang.ArrayIndexOutOfBoundsException");
        rule.setSeverity("critical");

        // --- Add rules at the top level ---
        reviewParameter.setRules(Collections.singletonList(rule));

        // --- FILE GROUP ---
        PFileGroup fileGroup = new PFileGroup();
        fileGroup.setPaths(Collections.singletonList("src/test/resources/com/quasarbyte/llm/codereview/maven/plugin/examples/ExampleOne.java"));
        // Don't set rules on file group since they're now at the top level

        // --- REVIEW TARGET ---
        PReviewTarget target = new PReviewTarget();
        target.setFileGroups(Collections.singletonList(fileGroup));

        reviewParameter.setTargets(Collections.singletonList(target));

        // --- LLM CLIENT CONFIGURATION ---
        PLlmClientConfiguration clientConfig = new PLlmClientConfiguration();
        int port = mockWebServer.getPort();
        String baseUrl = String.format("http://localhost:%d/v1/", port);
        clientConfig.setBaseUrl(baseUrl);
        clientConfig.setApiKey("demo");

        // --- BUILD FAILURE CONFIGURATION ---
        PBuildFailureConfiguration buildFailureConfiguration = new PBuildFailureConfiguration();
        buildFailureConfiguration.setWarningThreshold(0);
        buildFailureConfiguration.setCriticalThreshold(0); // Set to 0, so the test does not fail (1 will fail if there is a critical)

        // --- REPORTS CONFIGURATION ---
        PReportsConfiguration reportsConfiguration = new PReportsConfiguration();
        // Don't set any file paths, so no reports will be generated

        // --- Set parameters into Mojo ---
        mojo.setReviewParameter(reviewParameter);
        mojo.setLlmClientConfiguration(clientConfig);
        mojo.setBuildFailureConfiguration(buildFailureConfiguration);
        mojo.setReportsConfiguration(reportsConfiguration);

        // --- Run the pipeline ---
        assertDoesNotThrow(mojo::execute);

        // --- Verify HTTP requests ---
        try {
            // POST /v1/chat/completions
            RecordedRequest requestOne = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertNotNull(requestOne, "No HTTP request to mock server (POST /v1/chat/completions)");
            Assertions.assertEquals("/v1/chat/completions", requestOne.getPath(), "Second request path should be /v1/chat/completions");
            Assertions.assertEquals("POST", requestOne.getMethod(), "Second request should be POST");

            // Check the body of the POST request
            String body = requestOne.getBody().readUtf8();
            logger.info("body is {}", body);
            assertTrue(body.contains("ExampleOne.java"));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper method to load test resource files as strings (Java 8 compatible)
     */
    private String loadTestResource(String resourcePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(resourcePath)), StandardCharsets.UTF_8);
    }
}
