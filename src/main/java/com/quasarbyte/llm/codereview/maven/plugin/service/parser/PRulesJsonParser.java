package com.quasarbyte.llm.codereview.maven.plugin.service.parser;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;

import java.io.IOException;
import java.util.List;

public interface PRulesJsonParser {
    List<PRule> parseRules(String json) throws Exception;
}
