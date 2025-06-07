package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PLlmQuota;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmQuota;

public interface PLlmQuotaMapper {
    LlmQuota map(PLlmQuota quota);
}
