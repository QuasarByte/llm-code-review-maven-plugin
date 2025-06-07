package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;

public interface PRuleMapper {
    Rule map(PRule rule);
}
