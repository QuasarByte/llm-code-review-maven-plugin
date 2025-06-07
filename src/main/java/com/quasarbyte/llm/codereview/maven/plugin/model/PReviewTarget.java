package com.quasarbyte.llm.codereview.maven.plugin.model;

import java.util.List;

/**
 * Represents a single review target for code review.
 */
public class PReviewTarget {
    /**
     * The name of the review target.
     */
    private String reviewTargetName;

    /**
     * The list of file groups associated with this review target.
     * <p>
     * Each file group defines a set of files to be reviewed.
     * </p>
     */
    private List<PFileGroup> fileGroups;

    /**
     * The list of rules to be applied specifically to this review target.
     */
    private List<PRule> rules;

    /**
     * The paths to an external files containing rules for this review target.
     */
    private List<String> rulesFilePaths;

    /**
     * List of prompt messages used during the review of this target.
     */
    private List<String> reviewTargetPrompts;

    public String getReviewTargetName() {
        return reviewTargetName;
    }

    public PReviewTarget setReviewTargetName(String reviewTargetName) {
        this.reviewTargetName = reviewTargetName;
        return this;
    }

    public List<PFileGroup> getFileGroups() {
        return fileGroups;
    }

    public PReviewTarget setFileGroups(List<PFileGroup> fileGroups) {
        this.fileGroups = fileGroups;
        return this;
    }

    public List<PRule> getRules() {
        return rules;
    }

    public PReviewTarget setRules(List<PRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<String> getRulesFilePaths() {
        return rulesFilePaths;
    }

    public PReviewTarget setRulesFilePaths(List<String> rulesFilePaths) {
        this.rulesFilePaths = rulesFilePaths;
        return this;
    }

    public List<String> getReviewTargetPrompts() {
        return reviewTargetPrompts;
    }

    public PReviewTarget setReviewTargetPrompts(List<String> reviewTargetPrompts) {
        this.reviewTargetPrompts = reviewTargetPrompts;
        return this;
    }
}
