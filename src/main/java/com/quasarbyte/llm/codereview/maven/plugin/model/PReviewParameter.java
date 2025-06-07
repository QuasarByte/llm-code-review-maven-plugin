package com.quasarbyte.llm.codereview.maven.plugin.model;

import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmChatCompletionConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmQuota;

import java.util.List;

/**
 * Parameters for configuring a code review process.
 * <p>
 * This class defines the main review attributes, such as the review name, rules,
 * file targets, prompts, and specific configurations for LLM and custom scripting.
 * It also supports batching, timeouts, and integration with various code review targets.
 * </p>
 */
public class PReviewParameter {
    /**
     * The name of the review.
     */
    private String reviewName;

    /**
     * List of rules to be applied during the review.
     */
    private List<PRule> rules;

    /**
     * The paths to an external files containing rules for this review.
     */
    private List<String> rulesFilePaths;

    /**
     * List of review targets, such as files or directories to be reviewed.
     */
    private List<PReviewTarget> targets;

    /**
     * List of system prompt messages to be sent before the review.
     */
    private List<String> systemPrompts;

    /**
     * List of review prompt messages to be used during the review process.
     */
    private List<String> reviewPrompts;

    /**
     * Configuration for the LLM chat completion.
     */
    private LlmChatCompletionConfiguration llmChatCompletionConfiguration;

    /**
     * Configuration for Rhino, if used during the review.
     */
    private PRhinoConfiguration rhinoConfiguration;

    /**
     * Number of rules to process in a single batch during the review.
     */
    private Integer rulesBatchSize;

    /**
     * Timeout duration for the review operation, in ISO-8601 duration format.
     * <p>
     * Example: <code>PT30S</code> for 30 seconds, <code>PT5M</code> for 5 minutes.
     * See {@link java.time.Duration#parse(CharSequence)} for details.
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-">Duration.parse documentation</a>
     * </p>
     */
    private String timeoutDuration;

    /**
     * The quota configuration for LLM (Large Language Model) requests.
     * <p>
     * This field defines the quota limits for interactions with the LLM.
     * </p>
     */
    private PLlmQuota llmQuota;

    public String getReviewName() {
        return reviewName;
    }

    public PReviewParameter setReviewName(String reviewName) {
        this.reviewName = reviewName;
        return this;
    }

    public List<PRule> getRules() {
        return rules;
    }

    public PReviewParameter setRules(List<PRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<String> getRulesFilePaths() {
        return rulesFilePaths;
    }

    public PReviewParameter setRulesFilePaths(List<String> rulesFilePaths) {
        this.rulesFilePaths = rulesFilePaths;
        return this;
    }

    public List<PReviewTarget> getTargets() {
        return targets;
    }

    public PReviewParameter setTargets(List<PReviewTarget> targets) {
        this.targets = targets;
        return this;
    }

    public List<String> getSystemPrompts() {
        return systemPrompts;
    }

    public PReviewParameter setSystemPrompts(List<String> systemPrompts) {
        this.systemPrompts = systemPrompts;
        return this;
    }

    public List<String> getReviewPrompts() {
        return reviewPrompts;
    }

    public PReviewParameter setReviewPrompts(List<String> reviewPrompts) {
        this.reviewPrompts = reviewPrompts;
        return this;
    }

    public LlmChatCompletionConfiguration getLlmChatCompletionConfiguration() {
        return llmChatCompletionConfiguration;
    }

    public PReviewParameter setLlmChatCompletionConfiguration(LlmChatCompletionConfiguration llmChatCompletionConfiguration) {
        this.llmChatCompletionConfiguration = llmChatCompletionConfiguration;
        return this;
    }

    public PRhinoConfiguration getRhinoConfiguration() {
        return rhinoConfiguration;
    }

    public PReviewParameter setRhinoConfiguration(PRhinoConfiguration rhinoConfiguration) {
        this.rhinoConfiguration = rhinoConfiguration;
        return this;
    }

    public Integer getRulesBatchSize() {
        return rulesBatchSize;
    }

    public PReviewParameter setRulesBatchSize(Integer rulesBatchSize) {
        this.rulesBatchSize = rulesBatchSize;
        return this;
    }

    public String getTimeoutDuration() {
        return timeoutDuration;
    }

    public PReviewParameter setTimeoutDuration(String timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    public PLlmQuota getLlmQuota() {
        return llmQuota;
    }

    public PReviewParameter setLlmQuota(PLlmQuota llmQuota) {
        this.llmQuota = llmQuota;
        return this;
    }
}
