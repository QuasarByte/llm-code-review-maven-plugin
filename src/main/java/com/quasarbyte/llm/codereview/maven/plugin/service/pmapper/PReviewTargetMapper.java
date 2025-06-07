package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PReviewTarget;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewTarget;

public interface PReviewTargetMapper {
    ReviewTarget map(PReviewTarget reviewTarget);
}
