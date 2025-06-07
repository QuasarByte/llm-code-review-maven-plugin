package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PRuleMapper;
import com.quasarbyte.llm.codereview.sdk.model.parameter.Rule;
import com.quasarbyte.llm.codereview.sdk.model.parameter.RuleSeverityEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PRuleMapperImpl implements PRuleMapper {

    private static final Logger logger = LoggerFactory.getLogger(PRuleMapperImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Rule map(PRule rule) {
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Mapping PRule: {}", objectMapper.writeValueAsString(rule));
            } catch (Exception e) {
                logger.warn("Failed to serialize PRule for debug logging: {}", e.getMessage());
            }
        }

        if (rule == null) {
            logger.warn("Provided PRule is null, returning null.");
            return null;
        }

        Rule result = new Rule();
        result.setCode(rule.getCode());
        result.setDescription(rule.getDescription());
        result.setSeverity(getSeverity(rule));

        logger.info("Mapped PRule to Rule: code='{}', description='{}', severity={}",
                result.getCode(), result.getDescription(), result.getSeverity());

        return result;
    }

    private static RuleSeverityEnum getSeverity(PRule rule) {
        return rule.getSeverity() != null ? RuleSeverityEnum.fromName(rule.getSeverity()) : RuleSeverityEnum.INFO;
    }
}
