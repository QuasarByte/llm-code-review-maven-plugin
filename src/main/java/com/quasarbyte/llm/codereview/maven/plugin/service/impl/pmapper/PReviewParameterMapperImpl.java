package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewParameter;
import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewTarget;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.*;
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
        this.quotaMapper = quotaMapper;
        this.pReviewTargetMapper = pReviewTargetMapper;
        this.rhinoConfigurationMapper = rhinoConfigurationMapper;
        this.pRuleMapper = pRuleMapper;
        this.rulesFileReader = rulesFileReader;
        logger.debug("PReviewParameterMapperImpl initialized with dependencies.");
    }

    @Override
    public ReviewParameter map(PReviewParameter parameter) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Mapping PReviewParameter: {}", objectMapper.writeValueAsString(parameter));
            } catch (Exception e) {
                logger.warn("Failed to serialize PReviewParameter for debug logging: {}", e.getMessage());
            }
        }

        if (parameter == null) {
            logger.warn("Provided PReviewParameter is null, returning null.");
            return null;
        }
        ReviewParameter result = new ReviewParameter();
        result.setReviewName(parameter.getReviewName());

        final List<PRule> allPRules = new ArrayList<>();

        if (parameter.getRulesFilePaths() != null && !parameter.getRulesFilePaths().isEmpty()) {
            parameter.getRulesFilePaths().forEach(path -> {
                final List<PRule> pRules;
                if (notNullOrBlank(path)) {
                    try {
                        logger.info("Reading PRules from file: '{}'", path);
                        pRules = rulesFileReader.readPRules(path);
                        logger.debug("Read {} PRules from '{}'", pRules != null ? pRules.size() : 0, path);
                    } catch (Exception e) {
                        logger.error("Failed to read PRules from '{}': {}", path, e.getMessage(), e);
                        throw new LlmCodeReviewMavenPluginException(String.format("Can not read file: '%s', error message: '%s'", path, e.getMessage()), e);
                    }
                } else {
                    logger.warn("Skipped blank or null rulesFilePath.");
                    pRules = Collections.emptyList();
                }

                if (pRules != null) {
                    allPRules.addAll(pRules);
                }

            });
        }

        if (parameter.getRules() != null) {
            logger.debug("Adding {} inline PRules from parameter.", parameter.getRules().size());
            allPRules.addAll(parameter.getRules());
        }

        result.setRules(mapRules(allPRules));
        result.setTargets(mapTargets(parameter.getTargets()));
        result.setSystemPrompts(parameter.getSystemPrompts());
        result.setReviewPrompts(parameter.getReviewPrompts());

        LlmChatCompletionConfiguration llmChatCompletionConfiguration = parameter.getLlmChatCompletionConfiguration();

        if (llmChatCompletionConfiguration == null) {
            logger.error("reviewParameter.llmChatCompletionConfiguration is not set.");
            throw new ValidationException("reviewParameter.llmChatCompletionConfiguration is not set.");
        }

        if (nullOrBlank(llmChatCompletionConfiguration.getModel())) {
            logger.error("reviewParameter.llmChatCompletionConfiguration.model is not set.");
            throw new ValidationException("reviewParameter.llmChatCompletionConfiguration.model is not set.");
        }

        result.setLlmChatCompletionConfiguration(llmChatCompletionConfiguration);
        result.setLlmMessagesMapperConfiguration(getRhinoConfiguration(parameter));
        result.setRulesBatchSize(parameter.getRulesBatchSize());
        result.setTimeoutDuration(parseDuration(parameter.getTimeoutDuration()));
        result.setLlmQuota(quotaMapper.map(parameter.getLlmQuota()));

        logger.info("Successfully mapped PReviewParameter to ReviewParameter: reviewName='{}', total rules={}, total targets={}",
                result.getReviewName(),
                allPRules.size(),
                result.getTargets() != null ? result.getTargets().size() : 0);

        return result;
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

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
