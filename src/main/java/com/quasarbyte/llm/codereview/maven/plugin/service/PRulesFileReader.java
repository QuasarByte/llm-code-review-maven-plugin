package com.quasarbyte.llm.codereview.maven.plugin.service;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;

import java.util.List;

public interface PRulesFileReader {
    List<PRule> readPRules(String filePath) throws Exception;
}
