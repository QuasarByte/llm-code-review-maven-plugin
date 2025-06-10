package com.quasarbyte.llm.codereview.maven.plugin.model;

public class PPersistenceConfiguration {
    private PDataSourceConfiguration dataSourceConfiguration;
    private Boolean persistFileContent;

    public PDataSourceConfiguration getDataSourceConfiguration() {
        return dataSourceConfiguration;
    }

    public PPersistenceConfiguration setDataSourceConfiguration(PDataSourceConfiguration dataSourceConfiguration) {
        this.dataSourceConfiguration = dataSourceConfiguration;
        return this;
    }

    public Boolean getPersistFileContent() {
        return persistFileContent;
    }

    public PPersistenceConfiguration setPersistFileContent(Boolean persistFileContent) {
        this.persistFileContent = persistFileContent;
        return this;
    }
}
