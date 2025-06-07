package com.quasarbyte.llm.codereview.maven.plugin.service;

import com.quasarbyte.llm.codereview.maven.plugin.model.SeverityStatistics;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;

public interface SeverityStatisticsCalculator {
    SeverityStatistics calculate(ReviewResult reviewResult);
}
