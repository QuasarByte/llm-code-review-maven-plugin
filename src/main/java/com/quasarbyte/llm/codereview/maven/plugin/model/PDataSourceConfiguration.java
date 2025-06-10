package com.quasarbyte.llm.codereview.maven.plugin.model;

import java.util.Map;

public class PDataSourceConfiguration {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private Map<String, String> properties;

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public PDataSourceConfiguration setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public PDataSourceConfiguration setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public PDataSourceConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public PDataSourceConfiguration setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public PDataSourceConfiguration setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }
}
