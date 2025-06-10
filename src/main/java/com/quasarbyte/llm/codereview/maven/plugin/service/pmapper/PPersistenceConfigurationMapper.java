package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PPersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;

public interface PPersistenceConfigurationMapper {
    PersistenceConfiguration map(PPersistenceConfiguration source);
}
