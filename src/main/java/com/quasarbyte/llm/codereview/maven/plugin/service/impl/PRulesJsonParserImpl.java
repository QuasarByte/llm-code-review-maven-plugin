package com.quasarbyte.llm.codereview.maven.plugin.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class PRulesJsonParserImpl implements PRulesJsonParser {

    private static final Logger logger = LoggerFactory.getLogger(PRulesJsonParserImpl.class);

    @Override
    public List<PRule> parseRules(String json) throws Exception {
        if (json == null || json.trim().isEmpty()) {
            logger.warn("Input JSON for PRules parsing is null or empty.");
            return Collections.emptyList();
        }

        logger.debug("Parsing PRules from JSON string of length {}.", json.length());
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<PRule> rules = mapper.readValue(json, new TypeReference<List<PRule>>() {
            });
            logger.info("Parsed {} PRules from JSON input.", rules.size());
            return rules;
        } catch (Exception e) {
            logger.error("Failed to parse PRules from JSON: {}", e.getMessage(), e);
            throw e;
        }
    }
}
