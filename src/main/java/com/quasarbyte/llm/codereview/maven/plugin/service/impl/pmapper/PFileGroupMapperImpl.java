package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PFileGroup;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PFileGroupMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PRuleMapper;
import com.quasarbyte.llm.codereview.sdk.model.parameter.FileGroup;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PFileGroupMapperImpl implements PFileGroupMapper {

    private static final Logger logger = LoggerFactory.getLogger(PFileGroupMapperImpl.class);

    private final PRuleMapper pRuleMapper;
    private final PRulesFileReader rulesFileReader;

    public PFileGroupMapperImpl(PRuleMapper pRuleMapper, PRulesFileReader rulesFileReader) {
        this.pRuleMapper = pRuleMapper;
        this.rulesFileReader = rulesFileReader;
        logger.debug("PFileGroupMapperImpl initialized with PRuleMapper and PRulesFileReader.");
    }

    @Override
    public FileGroup map(PFileGroup fileGroup) {
        logger.info("Mapping PFileGroup: {}", fileGroup != null ? fileGroup.getFileGroupName() : "null");
        if (fileGroup == null) {
            logger.warn("Provided PFileGroup is null, returning null.");
            return null;
        }

        final List<PRule> allPRules = new ArrayList<>();

        if (fileGroup.getRulesFilePaths() != null && !fileGroup.getRulesFilePaths().isEmpty()) {
            logger.debug("Processing rulesFilePaths: {}", fileGroup.getRulesFilePaths());
            fileGroup.getRulesFilePaths().forEach(path -> {
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
        } else {
            logger.debug("No rulesFilePaths to process.");
        }

        if (fileGroup.getRules() != null) {
            logger.debug("Adding {} inline PRules from fileGroup.", fileGroup.getRules().size());
            allPRules.addAll(fileGroup.getRules());
        }

        FileGroup result = new FileGroup();
        result.setFileGroupName(fileGroup.getFileGroupName());
        result.setPaths(fileGroup.getPaths());
        result.setFilesBatchSize(fileGroup.getFilesBatchSize());
        result.setRules(mapRules(allPRules));
        result.setFileGroupPrompts(fileGroup.getFileGroupPrompts());
        result.setCodePage(fileGroup.getCodePage());

        logger.info("Mapped FileGroup '{}': {} total rules, {} paths.",
                result.getFileGroupName(),
                allPRules.size(),
                result.getPaths() != null ? result.getPaths().size() : 0);

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

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
