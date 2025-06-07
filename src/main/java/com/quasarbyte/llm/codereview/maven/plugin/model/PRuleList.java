package com.quasarbyte.llm.codereview.maven.plugin.model;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Represents a list of rules used for code review.
 * <p>
 * This class is typically used for XML/JSON serialization/deserialization
 * of multiple {@link PRule} elements.
 * </p>
 */
@XmlRootElement(name = "rules")
@XmlAccessorType(XmlAccessType.FIELD)
public class PRuleList {
    /**
     * The list of individual rules to be applied during code review.
     */
    @XmlElement(name = "rule")
    private List<PRule> rules;

    public List<PRule> getRules() {
        return rules;
    }

    public void setRules(List<PRule> rules) {
        this.rules = rules;
    }
}
