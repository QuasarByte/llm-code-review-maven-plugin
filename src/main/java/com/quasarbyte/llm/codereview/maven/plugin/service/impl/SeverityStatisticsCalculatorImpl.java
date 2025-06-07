package com.quasarbyte.llm.codereview.maven.plugin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.model.SeverityStatistics;
import com.quasarbyte.llm.codereview.maven.plugin.service.SeverityStatisticsCalculator;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewComment;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResultItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class SeverityStatisticsCalculatorImpl implements SeverityStatisticsCalculator {

    private static final Logger logger = LoggerFactory.getLogger(SeverityStatisticsCalculatorImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SeverityStatistics calculate(ReviewResult reviewResult) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Calculating severity statistics for ReviewResult: {}", objectMapper.writeValueAsString(reviewResult));
            } catch (Exception e) {
                logger.warn("Failed to serialize ReviewResult for logging: {}", e.getMessage());
            }
        }

        Map<RuleSeverityEnum, Long> countSeverityStatistics = countSeverityStatistics(reviewResult);

        long infoCount = Optional.ofNullable(countSeverityStatistics.get(RuleSeverityEnum.INFO)).orElse(0L);
        long warningCount = Optional.ofNullable(countSeverityStatistics.get(RuleSeverityEnum.WARNING)).orElse(0L);
        long criticalCount = Optional.ofNullable(countSeverityStatistics.get(RuleSeverityEnum.CRITICAL)).orElse(0L);

        logger.info("Calculated Severity Statistics - info: {}, warning: {}, critical: {}", infoCount, warningCount, criticalCount);

        SeverityStatistics result = new SeverityStatistics()
                .setInfoCount(infoCount)
                .setWarningCount(warningCount)
                .setCriticalCount(criticalCount);

        if (logger.isTraceEnabled()) {
            try {
                logger.trace("SeverityStatistics result: {}", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                logger.warn("Failed to serialize SeverityStatistics for logging: {}", e.getMessage());
            }
        }

        return result;
    }

    private static Map<RuleSeverityEnum, Long> countSeverityStatistics(ReviewResult reviewResult) {
        if (reviewResult == null) {
            logger.warn("ReviewResult is null in countSeverityStatistics. Returning empty statistics.");
            return Collections.emptyMap();
        }
        return Optional.of(reviewResult)
                .map(ReviewResult::getItems)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .map(ReviewResultItem::getComments)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(ReviewComment::getRule)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Rule::getSeverity, Collectors.counting()));
    }
}
