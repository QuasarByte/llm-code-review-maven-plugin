package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRhinoConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmMessagesMapperConfigurationRhino;

public interface PRhinoConfigurationMapper {
    LlmMessagesMapperConfigurationRhino map(PRhinoConfiguration configuration);
}
