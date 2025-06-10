package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewParameter;
import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewTarget;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation.MapperValidationUtils;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PReviewParameterMapperImpl implements PReviewParameterMapper {

    private static final Logger logger = LoggerFactory.getLogger(PReviewParameterMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PLlmQuotaMapper quotaMapper;
    private final PReviewTargetMapper pReviewTargetMapper;
    private final PRhinoConfigurationMapper rhinoConfigurationMapper;
    private final PRuleMapper pRuleMapper;
    private final PRulesFileReader rulesFileReader;

    public PReviewParameterMapperImpl(PLlmQuotaMapper quotaMapper,
                                      PReviewTargetMapper pReviewTargetMapper,
                                      PRhinoConfigurationMapper rhinoConfigurationMapper,
                                      PRuleMapper pRuleMapper,
                                      PRulesFileReader rulesFileReader) {
        // Enhanced validation for constructor dependencies
        MapperValidationUtils.requireNonNull(quotaMapper, "PLlmQuotaMapper");
        MapperValidationUtils.requireNonNull(pReviewTargetMapper, "PReviewTargetMapper");
        MapperValidationUtils.requireNonNull(rhinoConfigurationMapper, "PRhinoConfigurationMapper");
        MapperValidationUtils.requireNonNull(pRuleMapper, "PRuleMapper");
        MapperValidationUtils.requireNonNull(rulesFileReader, "PRulesFileReader");
        
        this.quotaMapper = quotaMapper;
        this.pReviewTargetMapper = pReviewTargetMapper;
        this.rhinoConfigurationMapper = rhinoConfigurationMapper;
        this.pRuleMapper = pRuleMapper;
        this.rulesFileReader = rulesFileReader;
        logger.debug("PReviewParameterMapperImpl initialized with all dependencies validated.");
    }

    @Override
    public ReviewParameter map(PReviewParameter parameter) {
        logger.debug("Starting review parameter mapping");
        
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Mapping PReviewParameter: {}", objectMapper.writeValueAsString(parameter));
            } catch (Exception e) {
                logger.warn("Failed to serialize PReviewParameter for debug logging: {}", e.getMessage());
            }
        }

        if (parameter == null) {
            logger.debug("Provided PReviewParameter is null, returning null");
            return null;
        }
        
        try {
            // Enhanced validation
            validateReviewParameter(parameter);
            
            ReviewParameter result = new ReviewParameter();
            
            // Map review name with trimming
            String reviewName = MapperValidationUtils.safeTrim(parameter.getReviewName());
            result.setReviewName(reviewName);
            logger.debug("Set reviewName: {}", reviewName != null ? reviewName : "[NOT PROVIDED]");

            // Process rules with enhanced error handling
            final List<PRule> allPRules = processRulesWithValidation(parameter);
            result.setRules(mapRules(allPRules));

            // Map targets with validation
            result.setTargets(mapTargets(parameter.getTargets()));
            
            // Map prompts with null safety
            result.setSystemPrompts(parameter.getSystemPrompts());
            result.setReviewPrompts(parameter.getReviewPrompts());
            logger.debug("Set system prompts: {}, review prompts: {}", 
                        parameter.getSystemPrompts() != null ? parameter.getSystemPrompts().size() : 0,
                        parameter.getReviewPrompts() != null ? parameter.getReviewPrompts().size() : 0);

            // Map LLM chat completion configuration with enhanced validation
            LlmChatCompletionConfiguration llmChatCompletionConfiguration = parameter.getLlmChatCompletionConfiguration();
            validateLlmChatCompletionConfiguration(llmChatCompletionConfiguration);
            result.setLlmChatCompletionConfiguration(llmChatCompletionConfiguration);

            // Map Rhino configuration with error handling
            try {
                result.setLlmMessagesMapperConfiguration(getRhinoConfiguration(parameter));
                logger.debug("Mapped Rhino configuration successfully");
            } catch (Exception e) {
                logger.error("Failed to map Rhino configuration: {}", e.getMessage(), e);
                throw new ValidationException("Failed to map Rhino configuration: " + e.getMessage(), e);
            }
            
            // Map rules batch size with validation
            Integer rulesBatchSize = parameter.getRulesBatchSize();
            if (rulesBatchSize != null) {
                MapperValidationUtils.requireInRange(rulesBatchSize, 1, 1000, "rulesBatchSize");
            }
            result.setRulesBatchSize(rulesBatchSize);
            
            // Map timeout duration with enhanced parsing
            result.setTimeoutDuration(parseDurationWithValidation(parameter.getTimeoutDuration()));
            
            // Map LLM quota with error handling
            try {
                result.setLlmQuota(quotaMapper.map(parameter.getLlmQuota()));
                logger.debug("Mapped LLM quota successfully");
            } catch (Exception e) {
                logger.error("Failed to map LLM quota: {}", e.getMessage(), e);
                throw new ValidationException("Failed to map LLM quota: " + e.getMessage(), e);
            }
            
            // Map use reasoning flag
            result.setUseReasoning(parameter.getUseReasoning());
            logger.debug("Set useReasoning: {}", parameter.getUseReasoning());

            MapperValidationUtils.logMappingSuccess("PReviewParameter", "ReviewParameter", 
                    String.format("reviewName: %s, rules: %d, targets: %d", 
                                 reviewName != null ? reviewName : "none", 
                                 allPRules.size(),
                                 result.getTargets() != null ? result.getTargets().size() : 0));

            return result;
            
        } catch (ValidationException e) {
            logger.error("Validation error during review parameter mapping: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during review parameter mapping: {}", e.getMessage(), e);
            throw new ValidationException("Failed to map review parameter: " + e.getMessage(), e);
        }
    }

    private List<Rule> mapRules(List<PRule> rules) {
        if (rules == null) {
            logger.debug("No PRules to map.");
            return Collections.emptyList();
        }
        logger.debug("Mapping {} PRules to Rules.", rules.size());
        return rules.stream()
                .map(pRuleMapper::map)
                .collect(Collectors.toList());
    }

    private List<ReviewTarget> mapTargets(List<PReviewTarget> targets) {
        if (targets == null) {
            logger.debug("No targets to map.");
            return Collections.emptyList();
        }
        logger.debug("Mapping {} PReviewTargets to ReviewTargets.", targets.size());
        return targets.stream()
                .map(pReviewTargetMapper::map)
                .collect(Collectors.toList());
    }

    private LlmMessagesMapperConfigurationRhino getRhinoConfiguration(PReviewParameter parameter) {
        return rhinoConfigurationMapper.map(parameter.getRhinoConfiguration());
    }

    private static Duration parseDuration(String duration) {
        if (duration == null) {
            return null;
        }
        try {
            return Duration.parse(duration);
        } catch (Exception e) {
            logger.error("Failed to parse duration '{}': {}", duration, e.getMessage());
            throw new LlmCodeReviewMavenPluginException("Can not parse duration, see 'https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-', error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Enhanced duration parsing with validation.
     */
    private Duration parseDurationWithValidation(String duration) {
        if (duration == null) {
            return null;
        }
        
        try {
            String trimmed = duration.trim();
            if (trimmed.isEmpty()) {
                logger.warn("Empty duration string provided, returning null");
                return null;
            }
            
            Duration result = Duration.parse(trimmed);
            
            // Validate duration is reasonable (not negative, not too long)
            if (result.isNegative()) {
                throw new ValidationException("Duration cannot be negative: " + duration);
            }
            
            // Prevent extremely long durations (e.g., more than 1 day)
            if (result.toDays() > 1) {
                logger.warn("Duration is very long ({}), this may cause issues", duration);
            }
            
            logger.debug("Parsed duration successfully: {} -> {}", duration, result);
            return result;
            
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to parse duration '{}': {}", duration, e.getMessage());
            throw new LlmCodeReviewMavenPluginException("Can not parse duration, see 'https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-', error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the review parameter input.
     */
    private void validateReviewParameter(PReviewParameter parameter) {
        logger.debug("Validating PReviewParameter");
        
        MapperValidationUtils.requireNonNull(parameter, "PReviewParameter");
        
        // Validate that we have at least one source of rules (at any level)
        boolean hasRulesSources = hasRulesAtAnyLevel(parameter);
        
        if (!hasRulesSources) {
            throw new ValidationException("At least one rules source must be provided (rulesFilePaths or inline rules at any level)");
        }
        
        // Validate targets are provided
        if (parameter.getTargets() == null || parameter.getTargets().isEmpty()) {
            throw new ValidationException("At least one review target must be provided");
        }
        
        logger.debug("PReviewParameter validation completed successfully");
    }
    
    /**
     * Checks if there are rules defined at any level (top-level, target-level, or file group-level).
     */
    private boolean hasRulesAtAnyLevel(PReviewParameter parameter) {
        // Check top-level rules
        boolean hasTopLevelRules = (parameter.getRulesFilePaths() != null && !parameter.getRulesFilePaths().isEmpty()) ||
                                  (parameter.getRules() != null && !parameter.getRules().isEmpty());
        
        if (hasTopLevelRules) {
            logger.debug("Found rules at top level");
            return true;
        }
        
        // Check target-level and file group-level rules
        if (parameter.getTargets() != null) {
            for (PReviewTarget target : parameter.getTargets()) {
                if (target == null) continue;
                
                // Check target-level rules
                boolean hasTargetRules = (target.getRulesFilePaths() != null && !target.getRulesFilePaths().isEmpty()) ||
                                        (target.getRules() != null && !target.getRules().isEmpty());
                
                if (hasTargetRules) {
                    logger.debug("Found rules at target level: {}", target.getReviewTargetName());
                    return true;
                }
                
                // Check file group-level rules
                if (target.getFileGroups() != null) {
                    for (com.quasarbyte.llm.codereview.maven.plugin.model.PFileGroup fileGroup : target.getFileGroups()) {
                        if (fileGroup == null) continue;
                        
                        boolean hasFileGroupRules = (fileGroup.getRulesFilePaths() != null && !fileGroup.getRulesFilePaths().isEmpty()) ||
                                                   (fileGroup.getRules() != null && !fileGroup.getRules().isEmpty());
                        
                        if (hasFileGroupRules) {
                            logger.debug("Found rules at file group level: {}", fileGroup.getFileGroupName());
                            return true;
                        }
                    }
                }
            }
        }
        
        logger.debug("No rules found at any level");
        return false;
    }
    
    /**
     * Validates LLM chat completion configuration.
     */
    private void validateLlmChatCompletionConfiguration(LlmChatCompletionConfiguration config) {
        MapperValidationUtils.requireNonNull(config, "llmChatCompletionConfiguration");
        MapperValidationUtils.requireNonBlank(config.getModel(), "llmChatCompletionConfiguration.model");
        
        logger.debug("LLM chat completion configuration validated successfully");
    }
    
    /**
     * Processes rules from files and inline sources with enhanced validation.
     */
    private List<PRule> processRulesWithValidation(PReviewParameter parameter) {
        final List<PRule> allPRules = new ArrayList<>();

        // Process rules from files
        if (parameter.getRulesFilePaths() != null && !parameter.getRulesFilePaths().isEmpty()) {
            logger.debug("Processing {} rules file paths", parameter.getRulesFilePaths().size());
            
            for (String path : parameter.getRulesFilePaths()) {
                if (MapperValidationUtils.notNullOrBlank(path)) {
                    try {
                        logger.info("Reading Rules from file: '{}'", path);
                        List<PRule> pRules = rulesFileReader.readPRules(path.trim());
                        
                        if (pRules != null && !pRules.isEmpty()) {
                            allPRules.addAll(pRules);
                            logger.debug("Read {} PRules from '{}'", pRules.size(), path);
                        } else {
                            logger.warn("No rules found in file: '{}'", path);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to read PRules from '{}': {}", path, e.getMessage(), e);
                        throw new LlmCodeReviewMavenPluginException(String.format("Can not read file: '%s', error message: '%s'", path, e.getMessage()), e);
                    }
                } else {
                    logger.warn("Skipped blank or null rulesFilePath");
                }
            }
        }

        // Process inline rules
        if (parameter.getRules() != null && !parameter.getRules().isEmpty()) {
            logger.debug("Adding {} inline PRules from parameter", parameter.getRules().size());
            allPRules.addAll(parameter.getRules());
        }
        
        logger.info("Processed total of {} rules from all sources", allPRules.size());
        return allPRules;
    }

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
