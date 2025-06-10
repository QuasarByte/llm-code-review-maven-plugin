package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.model.PDataSourceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;

public interface PDataSourceConfigurationMapper {
    DataSourceConfiguration map(PDataSourceConfiguration source);
}
