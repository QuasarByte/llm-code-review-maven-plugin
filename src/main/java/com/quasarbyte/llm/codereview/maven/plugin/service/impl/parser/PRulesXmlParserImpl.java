package com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser;

import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRuleList;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

public class PRulesXmlParserImpl implements PRulesXmlParser {

    private static final Logger logger = LoggerFactory.getLogger(PRulesXmlParserImpl.class);

    @Override
    public List<PRule> parseRules(String xml) throws Exception {
        logger.info("Parsing rules from XML. XML length: {}", xml != null ? xml.length() : "null");

        if (xml == null) {
            return Collections.emptyList();
        }

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(PRuleList.class);
            logger.debug("Created JAXBContext for PRuleList.");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            logger.debug("Created Unmarshaller for PRuleList.");
            PRuleList ruleList = (PRuleList) unmarshaller.unmarshal(new StringReader(xml));
            if (ruleList == null) {
                logger.warn("Parsed PRuleList is null.");
                return null;
            }
            List<PRule> rules = ruleList.getRules();
            logger.info("Successfully parsed {} rules from XML.", rules != null ? rules.size() : 0);
            return rules;
        } catch (Exception e) {
            logger.error("Failed to parse rules from XML: {}", e.getMessage(), e);
            throw e;
        }
    }
}
