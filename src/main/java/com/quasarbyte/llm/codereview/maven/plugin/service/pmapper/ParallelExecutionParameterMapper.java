package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;

public interface ParallelExecutionParameterMapper {
    ParallelExecutionParameter map(PParallelExecutionParameter parameter);
}
