package com.quasarbyte.llm.codereview.maven.plugin.service;

import com.quasarbyte.llm.codereview.maven.plugin.model.PBuildFailureConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.model.SeverityStatistics;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

public interface BuildFailureChecker {
    boolean check(PBuildFailureConfiguration configuration, SeverityStatistics severityStatistics);
}
