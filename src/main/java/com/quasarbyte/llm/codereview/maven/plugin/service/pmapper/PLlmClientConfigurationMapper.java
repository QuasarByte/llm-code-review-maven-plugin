package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PLlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;

import java.util.List;

public interface PLlmClientConfigurationMapper {
    LlmClientConfiguration map(PLlmClientConfiguration configuration);
    List<LlmClientConfiguration> map(List<PLlmClientConfiguration> configurations);
}
