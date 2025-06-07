package com.quasarbyte.llm.codereview.maven.plugin.exception;

public class LlmCodeReviewMavenPluginException extends RuntimeException {
    public LlmCodeReviewMavenPluginException(String message) {
        super(message);
    }

    public LlmCodeReviewMavenPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
