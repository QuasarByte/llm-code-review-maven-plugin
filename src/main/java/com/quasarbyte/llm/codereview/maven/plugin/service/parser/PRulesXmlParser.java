package com.quasarbyte.llm.codereview.maven.plugin.service.parser;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;

import java.util.List;

public interface PRulesXmlParser {
    List<PRule> parseRules(String xml) throws Exception;
}
