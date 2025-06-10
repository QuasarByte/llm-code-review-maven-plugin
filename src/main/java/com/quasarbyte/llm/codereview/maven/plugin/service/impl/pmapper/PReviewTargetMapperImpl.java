package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewTarget;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PFileGroupMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PReviewTargetMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PRuleMapper;
import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PReviewTargetMapperImpl implements PReviewTargetMapper {

    private static final Logger logger = LoggerFactory.getLogger(PReviewTargetMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final PFileGroupMapper pFileGroupMapper;
    private final PRuleMapper pRuleMapper;
    private final PRulesFileReader rulesFileReader;

    public PReviewTargetMapperImpl(PFileGroupMapper pFileGroupMapper, PRuleMapper pRuleMapper, PRulesFileReader rulesFileReader) {
        this.pFileGroupMapper = pFileGroupMapper;
        this.pRuleMapper = pRuleMapper;
        this.rulesFileReader = rulesFileReader;
        logger.debug("PReviewTargetMapperImpl initialized.");
    }

    @Override
    public ReviewTarget map(PReviewTarget reviewTarget) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Mapping PReviewTarget: {}", objectMapper.writeValueAsString(reviewTarget));
            } catch (Exception e) {
                logger.warn("Failed to serialize PReviewTarget for debug logging: {}", e.getMessage());
            }
        }

        if (reviewTarget == null) {
            logger.warn("Provided PReviewTarget is null, returning null.");
            return null;
        }

        ReviewTarget result = new ReviewTarget();
        result.setReviewTargetName(reviewTarget.getReviewTargetName());
        logger.debug("Set reviewTargetName: {}", reviewTarget.getReviewTargetName());

        result.setFileGroups(mapFileGroups(reviewTarget));
        logger.debug("Mapped file groups: {}", result.getFileGroups() != null ? result.getFileGroups().size() : 0);

        final List<PRule> allPRules = new ArrayList<>();

        if (reviewTarget.getRulesFilePaths() != null && !reviewTarget.getRulesFilePaths().isEmpty()) {
            logger.debug("Processing rulesFilePaths: {}", reviewTarget.getRulesFilePaths());
            reviewTarget.getRulesFilePaths().forEach(path -> {
                final List<PRule> pRules;
                if (notNullOrBlank(path)) {
                    try {
                        logger.info("Reading Rules from file: '{}'", path);
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

        if (reviewTarget.getRules() != null) {
            logger.debug("Adding {} inline PRules from reviewTarget.", reviewTarget.getRules().size());
            allPRules.addAll(reviewTarget.getRules());
        }

        result.setRules(mapRules(allPRules));
        logger.debug("Mapped total rules: {}", allPRules.size());

        result.setReviewTargetPrompts(reviewTarget.getReviewTargetPrompts());
        logger.debug("Set reviewTargetPrompts: {}", reviewTarget.getReviewTargetPrompts());

        logger.info("Successfully mapped PReviewTarget '{}' with {} fileGroups and {} rules.",
                result.getReviewTargetName(),
                result.getFileGroups() != null ? result.getFileGroups().size() : 0,
                allPRules.size());

        return result;
    }

    private List<FileGroup> mapFileGroups(PReviewTarget reviewTarget) {
        if (reviewTarget.getFileGroups() == null) {
            logger.debug("No file groups to map.");
            return Collections.emptyList();
        }
        logger.debug("Mapping {} file groups.", reviewTarget.getFileGroups().size());
        return reviewTarget.getFileGroups().stream()
                .map(pFileGroupMapper::map)
                .collect(Collectors.toList());
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

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
