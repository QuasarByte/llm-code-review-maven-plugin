package com.quasarbyte.llm.codereview.maven.plugin;

import com.quasarbyte.llm.codereview.maven.plugin.model.*;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LlmCodeReviewMojoIT {

    private static final String OLLAMA_BASE_URL = "http://localhost:11434/v1/";
    private static final String LM_STUDIO_BASE_URL = "http://127.0.0.1:1234/v1/";

    @Test
    void testExecuteWithMinimalConfig() {
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

        // --- FILE GROUP ---
        PFileGroup fileGroup = new PFileGroup();
        fileGroup.setPaths(Collections.singletonList("src/test/resources/com/quasarbyte/llm/codereview/maven/plugin/examples/ExampleOne.java"));
        fileGroup.setRules(Collections.singletonList(rule));

        // --- REVIEW TARGET ---
        PReviewTarget target = new PReviewTarget();
        target.setFileGroups(Collections.singletonList(fileGroup));

        reviewParameter.setTargets(Collections.singletonList(target));

        // --- LLM CLIENT CONFIGURATION ---
        PLlmClientConfiguration clientConfig = new PLlmClientConfiguration();
        clientConfig.setBaseUrl(LM_STUDIO_BASE_URL);
        clientConfig.setApiKey("demo");

        // --- BUILD FAILURE CONFIGURATION ---
        PBuildFailureConfiguration buildFailureConfiguration = new PBuildFailureConfiguration();
        buildFailureConfiguration.setWarningThreshold(0);
        buildFailureConfiguration.setCriticalThreshold(0); // Set to 0, so the test does not fail (1 will fail if there is a critical)

        // --- Set parameters into Mojo ---
        mojo.setReviewParameter(reviewParameter);
        mojo.setLlmClientConfiguration(clientConfig);
        mojo.setBuildFailureConfiguration(buildFailureConfiguration);

        // --- Run the pipeline ---
        assertDoesNotThrow(mojo::execute);
    }
}
