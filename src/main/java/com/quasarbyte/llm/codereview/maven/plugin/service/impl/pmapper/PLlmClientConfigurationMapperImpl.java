package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PLlmClientConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PLlmClientConfigurationMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PProxyMapper;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PLlmClientConfigurationMapperImpl implements PLlmClientConfigurationMapper {

    private static final Logger logger = LoggerFactory.getLogger(PLlmClientConfigurationMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PProxyMapper proxyMapper;

    public PLlmClientConfigurationMapperImpl(PProxyMapper proxyMapper) {
        this.proxyMapper = proxyMapper;
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("PLlmClientConfigurationMapperImpl initialized with PProxyMapper: {}",
                        objectMapper.writeValueAsString(proxyMapper));
            } catch (Exception e) {
                logger.warn("Failed to serialize PProxyMapper for debug logging: {}", e.getMessage());
            }
        }
    }

    @Override
    public LlmClientConfiguration map(PLlmClientConfiguration configuration) {
        logger.info("Mapping PLlmClientConfiguration: {}", configuration);
        if (configuration == null) {
            logger.warn("Provided PLlmClientConfiguration is null, returning null.");
            return null;
        }

        String baseUrl = configuration.getBaseUrl();

        if (nullOrBlank(baseUrl)) {
            logger.error("Validation failed: llmClientConfiguration.baseUrl is not set.");
            throw new ValidationException("llmClientConfiguration.baseUrl is not set.");
        }

        LlmClientConfiguration result = new LlmClientConfiguration();

        result.setCheckJacksonVersionCompatibility(configuration.getCheckJacksonVersionCompatibility());
        logger.debug("Set checkJacksonVersionCompatibility: {}", configuration.getCheckJacksonVersionCompatibility());

        result.setResponseValidation(configuration.getResponseValidation());
        logger.debug("Set responseValidation: {}", configuration.getResponseValidation());

        result.setTimeoutDuration(configuration.getTimeoutDuration());
        logger.debug("Set timeoutDuration: {}", configuration.getTimeoutDuration());

        result.setMaxRetries(configuration.getMaxRetries());
        logger.debug("Set maxRetries: {}", configuration.getMaxRetries());

        result.setJsonMapper(configuration.getJsonMapper());
        logger.debug("Set jsonMapper: {}", configuration.getJsonMapper());

        result.setHeadersMap(convertMap(configuration.getHeadersMap()));
        logger.debug("Set headersMap: {}", configuration.getHeadersMap());

        result.setQueryParamsMap(convertMap(configuration.getQueryParamsMap()));
        logger.debug("Set queryParamsMap: {}", configuration.getQueryParamsMap());

        result.setProxy(proxyMapper.map(configuration.getProxy()));
        logger.debug("Set proxy: {}", configuration.getProxy());

        result.setApiKey(configuration.getApiKey());
        logger.debug("Set apiKey: {}", configuration.getApiKey() != null ? "[PROVIDED]" : "[NOT PROVIDED]");

        result.setAzureServiceVersion(configuration.getAzureServiceVersion());
        logger.debug("Set azureServiceVersion: {}", configuration.getAzureServiceVersion());

        result.setBaseUrl(configuration.getBaseUrl());
        logger.debug("Set baseUrl: {}", configuration.getBaseUrl());

        result.setOrganization(configuration.getOrganization());
        logger.debug("Set organization: {}", configuration.getOrganization());

        result.setProject(configuration.getProject());
        logger.debug("Set project: {}", configuration.getProject());

        logger.info("Successfully mapped PLlmClientConfiguration to LlmClientConfiguration with baseUrl: '{}'", result.getBaseUrl());
        return result;
    }

    private static Map<String, Iterable<String>> convertMap(Map<String, List<String>> map) {
        if (map == null) {
            logger.debug("convertMap: input map is null, returning null.");
            return null;
        }
        logger.debug("convertMap: converting map with {} entries.", map.size());
        return new HashMap<>(map);
    }

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
