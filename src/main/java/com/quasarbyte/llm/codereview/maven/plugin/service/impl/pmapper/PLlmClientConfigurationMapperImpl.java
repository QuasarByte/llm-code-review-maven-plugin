package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PLlmClientConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PLlmClientConfigurationMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PProxyMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation.MapperValidationUtils;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Enhanced mapper implementation for PLlmClientConfiguration to LlmClientConfiguration.
 * Provides comprehensive error handling, validation, and performance optimizations.
 */
public class PLlmClientConfigurationMapperImpl implements PLlmClientConfigurationMapper {

    private static final Logger logger = LoggerFactory.getLogger(PLlmClientConfigurationMapperImpl.class);

    private final PProxyMapper proxyMapper;

    public PLlmClientConfigurationMapperImpl(PProxyMapper proxyMapper) {
        MapperValidationUtils.requireNonNull(proxyMapper, "PProxyMapper");
        this.proxyMapper = proxyMapper;
        logger.debug("PLlmClientConfigurationMapperImpl initialized with PProxyMapper: {}", 
                    proxyMapper.getClass().getSimpleName());
    }

    @Override
    public LlmClientConfiguration map(PLlmClientConfiguration configuration) {
        logger.debug("Starting LLM client configuration mapping");
        
        if (configuration == null) {
            logger.debug("Provided PLlmClientConfiguration is null, returning null");
            return null;
        }

        try {
            // Validate input before mapping
            validateLlmClientConfiguration(configuration);
            
            LlmClientConfiguration result = new LlmClientConfiguration();

            // Map base URL (required field)
            String baseUrl = MapperValidationUtils.safeTrim(configuration.getBaseUrl());
            MapperValidationUtils.requireValidHttpUrl(baseUrl, "baseUrl");
            result.setBaseUrl(baseUrl);
            MapperValidationUtils.logValidationSuccess("baseUrl", baseUrl);

            // Map optional boolean fields with null safety
            result.setCheckJacksonVersionCompatibility(configuration.getCheckJacksonVersionCompatibility());
            logger.debug("Set checkJacksonVersionCompatibility: {}", configuration.getCheckJacksonVersionCompatibility());

            result.setResponseValidation(configuration.getResponseValidation());
            logger.debug("Set responseValidation: {}", configuration.getResponseValidation());

            // Map timeout duration with validation
            Duration timeoutDuration = configuration.getTimeoutDuration();
            if (timeoutDuration != null) {
                // Convert Duration to milliseconds for validation
                long timeoutMillis = timeoutDuration.toMillis();
                if (timeoutMillis <= 0 || timeoutMillis > 300000) {
                    throw new ValidationException("timeoutDuration must be between 1ms and 300000ms (5 minutes), but was: " + timeoutMillis + "ms");
                }
                result.setTimeoutDuration(timeoutDuration);
                logger.debug("Set timeoutDuration: {}", timeoutDuration);
            }

            // Map max retries with validation
            Integer maxRetries = configuration.getMaxRetries();
            if (maxRetries != null) {
                MapperValidationUtils.requireInRange(maxRetries, 0, 10, "maxRetries"); // 0 to 10 retries
                result.setMaxRetries(maxRetries);
                logger.debug("Set maxRetries: {}", maxRetries);
            }

            // Map headers with enhanced validation
            Map<String, List<String>> headersMap = configuration.getHeadersMap();
            if (headersMap != null) {
                Map<String, Iterable<String>> validatedHeaders = validateAndConvertHeaders(headersMap, "headersMap");
                result.setHeadersMap(validatedHeaders);
                logger.debug("Set headersMap with {} entries", validatedHeaders.size());
            }

            // Map query parameters with enhanced validation
            Map<String, List<String>> queryParamsMap = configuration.getQueryParamsMap();
            if (queryParamsMap != null) {
                Map<String, Iterable<String>> validatedQueryParams = validateAndConvertHeaders(queryParamsMap, "queryParamsMap");
                result.setQueryParamsMap(validatedQueryParams);
                logger.debug("Set queryParamsMap with {} entries", validatedQueryParams.size());
            }

            // Map proxy configuration with error handling
            try {
                result.setProxy(proxyMapper.map(configuration.getProxy()));
                logger.debug("Set proxy configuration: {}", configuration.getProxy() != null ? "provided" : "not provided");
            } catch (Exception e) {
                logger.error("Failed to map proxy configuration: {}", e.getMessage(), e);
                throw new ValidationException("Failed to map proxy configuration: " + e.getMessage(), e);
            }

            // Map API key (sensitive field)
            String apiKey = configuration.getApiKey();
            result.setApiKey(apiKey);
            logger.debug("Set apiKey: {}", apiKey != null ? "[PROVIDED]" : "[NOT PROVIDED]");

            // Map Azure service version
            String azureServiceVersion = MapperValidationUtils.safeTrim(configuration.getAzureServiceVersion());
            result.setAzureServiceVersion(azureServiceVersion);
            logger.debug("Set azureServiceVersion: {}", azureServiceVersion != null ? azureServiceVersion : "[NOT PROVIDED]");

            // Map organization
            String organization = MapperValidationUtils.safeTrim(configuration.getOrganization());
            result.setOrganization(organization);
            logger.debug("Set organization: {}", organization != null ? organization : "[NOT PROVIDED]");

            // Map project
            String project = MapperValidationUtils.safeTrim(configuration.getProject());
            result.setProject(project);
            logger.debug("Set project: {}", project != null ? project : "[NOT PROVIDED]");

            MapperValidationUtils.logMappingSuccess("PLlmClientConfiguration", "LlmClientConfiguration", 
                    String.format("baseUrl: %s", MapperValidationUtils.maskSensitiveInfo(result.getBaseUrl())));
            
            return result;
            
        } catch (ValidationException e) {
            logger.error("Validation error during LLM client configuration mapping: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during LLM client configuration mapping: {}", e.getMessage(), e);
            throw new ValidationException("Failed to map LLM client configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LlmClientConfiguration> map(List<PLlmClientConfiguration> configurations) {
        logger.debug("Starting batch LLM client configuration mapping");
        
        if (configurations == null) {
            logger.debug("Provided List<PLlmClientConfiguration> is null, returning empty list");
            return Collections.emptyList();
        }
        
        if (configurations.isEmpty()) {
            logger.debug("Provided List<PLlmClientConfiguration> is empty, returning empty list");
            return Collections.emptyList();
        }
        
        try {
            logger.info("Mapping {} LLM client configurations", configurations.size());
            
            List<LlmClientConfiguration> result = configurations.stream()
                .map(this::map)
                .collect(Collectors.toList());
                
            MapperValidationUtils.logMappingSuccess("List<PLlmClientConfiguration>", "List<LlmClientConfiguration>", 
                    String.format("%d configurations", result.size()));
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error during batch LLM client configuration mapping: {}", e.getMessage(), e);
            throw new ValidationException("Failed to map LLM client configurations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the LLM client configuration input.
     * @param config the configuration to validate
     * @throws ValidationException if validation fails
     */
    private void validateLlmClientConfiguration(PLlmClientConfiguration config) {
        logger.debug("Validating PLlmClientConfiguration");
        
        MapperValidationUtils.requireNonNull(config, "PLlmClientConfiguration");
        
        // Validate base URL (required)
        MapperValidationUtils.requireNonBlank(config.getBaseUrl(), "baseUrl");
        
        // Validate timeout duration range if provided
        Duration timeoutDuration = config.getTimeoutDuration();
        if (timeoutDuration != null) {
            long timeoutMillis = timeoutDuration.toMillis();
            if (timeoutMillis <= 0 || timeoutMillis > 300000) {
                throw new ValidationException("timeoutDuration must be between 1ms and 300000ms, but was: " + timeoutMillis + "ms");
            }
        }
        
        // Validate max retries range if provided
        Integer maxRetries = config.getMaxRetries();
        if (maxRetries != null && (maxRetries < 0 || maxRetries > 10)) {
            throw new ValidationException("maxRetries must be between 0 and 10, but was: " + maxRetries);
        }
        
        logger.debug("PLlmClientConfiguration validation completed successfully");
    }
    
    /**
     * Validates and converts headers/query parameters map.
     * @param map the map to validate and convert
     * @param fieldName the field name for error messages
     * @return validated and converted map
     * @throws ValidationException if validation fails
     */
    private Map<String, Iterable<String>> validateAndConvertHeaders(Map<String, List<String>> map, String fieldName) {
        if (map == null) {
            return null;
        }
        
        Map<String, Iterable<String>> result = new HashMap<>();
        
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            // Validate key
            if (MapperValidationUtils.nullOrBlank(key)) {
                throw new ValidationException(String.format("%s contains null or blank key", fieldName));
            }
            
            // Validate values
            if (values == null) {
                logger.warn("{} contains null values for key: {}", fieldName, key);
                result.put(key.trim(), Collections.emptyList());
            } else {
                // Filter out null values and trim non-null ones
                List<String> cleanValues = values.stream()
                    .filter(v -> v != null)
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .collect(Collectors.toList());
                    
                result.put(key.trim(), cleanValues);
            }
        }
        
        logger.debug("Validated and converted {} with {} entries", fieldName, result.size());
        return result;
    }
}
