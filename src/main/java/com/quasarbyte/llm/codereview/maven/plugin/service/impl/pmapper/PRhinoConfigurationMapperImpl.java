package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRhinoConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.service.ResourceLoader;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PRhinoConfigurationMapper;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.service.LlmMessMapperRhinoConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PRhinoConfigurationMapperImpl implements PRhinoConfigurationMapper {

    private static final Logger logger = LoggerFactory.getLogger(PRhinoConfigurationMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository;
    private final ResourceLoader resourceLoader;

    public PRhinoConfigurationMapperImpl(LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository,
                                         ResourceLoader resourceLoader) {
        this.llmMessMapperRhinoConfigRepository = llmMessMapperRhinoConfigRepository;
        this.resourceLoader = resourceLoader;
        logger.debug("PRhinoConfigurationMapperImpl initialized with LlmMessMapperRhinoConfigRepository and ResourceLoader.");
    }

    @Override
    public LlmMessagesMapperConfigurationRhino map(PRhinoConfiguration configuration) {

        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Mapping PRhinoConfiguration: {}", objectMapper.writeValueAsString(configuration));
            } catch (Exception e) {
                logger.warn("Failed to serialize PRhinoConfiguration for debug logging: {}", e.getMessage());
            }
        }

        if (configuration != null) {

            if (notNullOrBlank(configuration.getScriptFilePath()) && notNullOrBlank(configuration.getFunctionName())) {
                logger.info("Mapping PRhinoConfiguration with scriptFilePath='{}' and functionName='{}'.",
                        configuration.getScriptFilePath(), configuration.getFunctionName());

                LlmMessagesMapperConfigurationRhino result = new LlmMessagesMapperConfigurationRhino();

                String scriptFilePath = configuration.getScriptFilePath();
                String scriptBody;

                try {
                    scriptBody = resourceLoader.load(scriptFilePath);
                    logger.debug("Loaded script body from '{}', length={}", scriptFilePath, scriptBody != null ? scriptBody.length() : 0);
                } catch (IOException e) {
                    logger.error("Failed to load script from '{}': {}", scriptFilePath, e.getMessage(), e);
                    throw new LlmCodeReviewMavenPluginException(String.format("Can not read script body by file path '%s', error: '%s'", scriptFilePath, e.getMessage()), e);
                }

                result.setScriptBody(scriptBody);
                result.setFunctionName(configuration.getFunctionName());

                logger.info("Successfully mapped PRhinoConfiguration with functionName='{}'.", configuration.getFunctionName());
                return result;

            } else {
                logger.error("Validation failed: Script file path and function name can not be blank.");
                throw new ValidationException("Script file path and function name can not be blank.");
            }

        } else {
            logger.info("PRhinoConfiguration is null, loading default configuration from repository.");
            return llmMessMapperRhinoConfigRepository.findDefaultConfiguration();
        }
    }

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
