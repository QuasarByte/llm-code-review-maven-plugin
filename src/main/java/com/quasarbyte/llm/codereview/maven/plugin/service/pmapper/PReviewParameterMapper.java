package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;

public interface PReviewParameterMapper {
    ReviewParameter map(PReviewParameter parameter);
}
