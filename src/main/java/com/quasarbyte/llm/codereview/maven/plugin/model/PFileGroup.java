package com.quasarbyte.llm.codereview.maven.plugin.model;

import java.util.List;

/**
 * Represents a group of files to be processed together during code review.
 * <p>
 * A file group allows you to specify a set of file paths, associate specific rules,
 * configure batch processing size, and set prompts or encoding options.
 * </p>
 */
public class PFileGroup {
    /**
     * The name of this file group.
     */
    private String fileGroupName;

    /**
     * The list of file paths or glob patterns included in this group.
     * Each entry can be a file path or a glob pattern.
     */
    private List<String> paths;

    /**
     * The list of file paths or glob patterns to be excluded from this group.
     * Each entry can be a file path or a glob pattern.
     */
    private List<String> excludePaths;

    /**
     * The number of files to process in a single batch within this group.
     * <p>
     * If {@code null} or less than or equal to zero, batching will not be applied.
     * </p>
     */
    private Integer filesBatchSize;

    /**
     * The list of rules to apply specifically to this file group.
     */
    private List<PRule> rules;

    /**
     * The paths to an external files containing rules for this file group.
     */
    private List<String> rulesFilePaths;

    /**
     * List of prompt messages to be used for this file group.
     */
    private List<String> fileGroupPrompts;

    /**
     * The character encoding (code page) to use for the files in this group.
     * <p>
     * Example: {@code UTF-8}, {@code Windows-1251}, etc.
     * </p>
     */
    private String codePage;

    public String getFileGroupName() {
        return fileGroupName;
    }

    public PFileGroup setFileGroupName(String fileGroupName) {
        this.fileGroupName = fileGroupName;
        return this;
    }

    public List<String> getPaths() {
        return paths;
    }

    public PFileGroup setPaths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    public List<String> getExcludePaths() {
        return excludePaths;
    }

    public PFileGroup setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
        return this;
    }

    public Integer getFilesBatchSize() {
        return filesBatchSize;
    }

    public PFileGroup setFilesBatchSize(Integer filesBatchSize) {
        this.filesBatchSize = filesBatchSize;
        return this;
    }

    public List<PRule> getRules() {
        return rules;
    }

    public PFileGroup setRules(List<PRule> rules) {
        this.rules = rules;
        return this;
    }

    public List<String> getRulesFilePaths() {
        return rulesFilePaths;
    }

    public PFileGroup setRulesFilePaths(List<String> rulesFilePaths) {
        this.rulesFilePaths = rulesFilePaths;
        return this;
    }

    public List<String> getFileGroupPrompts() {
        return fileGroupPrompts;
    }

    public PFileGroup setFileGroupPrompts(List<String> fileGroupPrompts) {
        this.fileGroupPrompts = fileGroupPrompts;
        return this;
    }

    public String getCodePage() {
        return codePage;
    }

    public PFileGroup setCodePage(String codePage) {
        this.codePage = codePage;
        return this;
    }
}
