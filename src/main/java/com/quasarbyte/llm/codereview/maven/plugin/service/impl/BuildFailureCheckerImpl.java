package com.quasarbyte.llm.codereview.maven.plugin.service.impl;

import com.quasarbyte.llm.codereview.maven.plugin.model.PBuildFailureConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.model.SeverityStatistics;
import com.quasarbyte.llm.codereview.maven.plugin.service.BuildFailureChecker;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class BuildFailureCheckerImpl implements BuildFailureChecker {

    private static final Logger logger = LoggerFactory.getLogger(BuildFailureCheckerImpl.class);

    @Override
    public boolean check(PBuildFailureConfiguration configuration, SeverityStatistics statistics) {
        if (configuration == null) {
            logger.warn("BuildFailureChecker: PBuildFailureConfiguration is null, build will NOT fail.");
            return false;
        }
        if (statistics == null) {
            logger.warn("BuildFailureChecker: SeverityStatistics is null, build will NOT fail.");
            return false;
        }

        boolean result = isBuildFailure(configuration, statistics);

        if (result) {
            logger.error("BuildFailureChecker: Build FAILURE detected according to the thresholds.");
        } else {
            logger.info("BuildFailureChecker: Build will NOT fail, all thresholds respected.");
        }

        return result;
    }

    private boolean isBuildFailure(PBuildFailureConfiguration configuration, SeverityStatistics statistics) {
        long infoCount = Optional.ofNullable(statistics.getInfoCount()).orElse(0L);
        long warningCount = Optional.ofNullable(statistics.getWarningCount()).orElse(0L);
        long criticalCount = Optional.ofNullable(statistics.getCriticalCount()).orElse(0L);

        logger.info("Found {} comments with severity '{}'", infoCount, RuleSeverityEnum.INFO.name().toLowerCase());
        logger.info("Found {} comments with severity '{}'", warningCount, RuleSeverityEnum.WARNING.name().toLowerCase());
        logger.info("Found {} comments with severity '{}'", criticalCount, RuleSeverityEnum.CRITICAL.name().toLowerCase());

        return isBuildFailure(configuration, warningCount, criticalCount);
    }

    private boolean isBuildFailure(PBuildFailureConfiguration configuration, long warningCount, long criticalCount) {
        Integer warningThreshold = configuration.getWarningThreshold();
        Integer criticalThreshold = configuration.getCriticalThreshold();

        logger.debug("Checking thresholds: warningThreshold={}, criticalThreshold={}, warningCount={}, criticalCount={}",
                warningThreshold, criticalThreshold, warningCount, criticalCount);

        // If a critical threshold is set and exceeded, build should fail
        if (criticalThreshold != null && criticalThreshold > 0 && criticalCount >= criticalThreshold) {
            logger.error("Build will fail: criticalCount {} >= criticalThreshold {}", criticalCount, criticalThreshold);
            return true;
        }

        // If a warning threshold is set and exceeded, build should fail
        if (warningThreshold != null && warningThreshold > 0 && warningCount >= warningThreshold) {
            logger.error("Build will fail: warningCount {} >= warningThreshold {}", warningCount, warningThreshold);
            return true;
        }

        // No failure conditions met
        logger.debug("No threshold exceeded: build will NOT fail.");
        return false;
    }
}
