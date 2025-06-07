package com.quasarbyte.llm.codereview.maven.plugin.model;

/**
 * Model representing the request quota for the LLM plugin.
 * <p>
 * This class is used to store and manage the request limit (quota)
 * applied when working with an LLM (Large Language Model) in the
 * CodeReview Maven plugin.
 * </p>
 */
public class PLlmQuota {

    /**
     * The maximum number of allowed requests (quota).
     */
    private Long requestQuota;

    /**
     * Gets the configured request quota.
     *
     * @return the maximum number of allowed requests (quota)
     */
    public Long getRequestQuota() {
        return requestQuota;
    }

    /**
     * Sets the request quota.
     *
     * @param requestQuota the maximum number of allowed requests (quota)
     * @return the current {@link PLlmQuota} instance for method chaining
     */
    public PLlmQuota setRequestQuota(Long requestQuota) {
        this.requestQuota = requestQuota;
        return this;
    }
}
