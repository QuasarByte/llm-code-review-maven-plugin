package com.quasarbyte.llm.codereview.maven.plugin.model;

/**
 * Configuration class for managing various report output formats and file paths.
 * <p>
 * This class provides configuration options for generating reports in multiple formats
 * including JSON, Markdown, HTML, and CSV. Each report type can be configured to output
 * to a specific file path or to standard output/error streams.
 * </p>
 * <p>
 * Special file path values:
 * <ul>
 *   <li>STDOUT - Output to standard output stream</li>
 *   <li>STDERR - Output to standard error stream</li>
 *   <li>Custom file path - Output to specified file location</li>
 * </ul>
 * </p>
 */
public class PReportsConfiguration {

    /**
     * JSON report file path (STDOUT, STDERR, report.json).
     * <p>
     * Specifies where the JSON format report should be written. Can be set to:
     * <ul>
     *   <li>STDOUT - Write to standard output</li>
     *   <li>STDERR - Write to standard error</li>
     *   <li>File path - Write to a specified file (e.g., "report.json")</li>
     * </ul>
     * </p>
     */
    private String jsonReportFilePath;

    /**
     * Markdown report file path (STDOUT, STDERR, report.md).
     * <p>
     * Specifies where the Markdown format report should be written. Can be set to:
     * <ul>
     *   <li>STDOUT - Write to standard output</li>
     *   <li>STDERR - Write to standard error</li>
     *   <li>File path - Write to a specified file (e.g., "report.md")</li>
     * </ul>
     * </p>
     */
    private String markdownReportFilePath;

    /**
     * HTML report file path (STDOUT, STDERR, report.html).
     * <p>
     * Specifies where the HTML format report should be written. Can be set to:
     * <ul>
     *   <li>STDOUT - Write to standard output</li>
     *   <li>STDERR - Write to standard error</li>
     *   <li>File path - Write to a specified file (e.g., "report.html")</li>
     * </ul>
     * </p>
     */
    private String htmlReportFilePath;

    /**
     * CSV report file path (STDOUT, STDERR, report.csv).
     * <p>
     * Specifies where the CSV format report should be written. Can be set to:
     * <ul>
     *   <li>STDOUT - Write to standard output</li>
     *   <li>STDERR - Write to standard error</li>
     *   <li>File path - Write to a specified file (e.g., "report.csv")</li>
     * </ul>
     * </p>
     */
    private String csvReportFilePath;

    /**
     * Gets the JSON report file path.
     *
     * @return the JSON report file path, or null if not set
     */
    public String getJsonReportFilePath() {
        return jsonReportFilePath;
    }

    /**
     * Sets the JSON report file path.
     *
     * @param jsonReportFilePath the JSON report file path to set
     * @return this configuration instance for method chaining
     */
    public PReportsConfiguration setJsonReportFilePath(String jsonReportFilePath) {
        this.jsonReportFilePath = jsonReportFilePath;
        return this;
    }

    /**
     * Gets the Markdown report file path.
     *
     * @return the Markdown report file path, or null if not set
     */
    public String getMarkdownReportFilePath() {
        return markdownReportFilePath;
    }

    /**
     * Sets the Markdown report file path.
     *
     * @param markdownReportFilePath the Markdown report file path to set
     * @return this configuration instance for method chaining
     */
    public PReportsConfiguration setMarkdownReportFilePath(String markdownReportFilePath) {
        this.markdownReportFilePath = markdownReportFilePath;
        return this;
    }

    /**
     * Gets the HTML report file path.
     *
     * @return the HTML report file path, or null if not set
     */
    public String getHtmlReportFilePath() {
        return htmlReportFilePath;
    }

    /**
     * Sets the HTML report file path.
     *
     * @param htmlReportFilePath the HTML report file path to set
     * @return this configuration instance for method chaining
     */
    public PReportsConfiguration setHtmlReportFilePath(String htmlReportFilePath) {
        this.htmlReportFilePath = htmlReportFilePath;
        return this;
    }

    /**
     * Gets the CSV report file path.
     *
     * @return the CSV report file path, or null if not set
     */
    public String getCsvReportFilePath() {
        return csvReportFilePath;
    }

    /**
     * Sets the CSV report file path.
     *
     * @param csvReportFilePath the CSV report file path to set
     * @return this configuration instance for method chaining
     */
    public PReportsConfiguration setCsvReportFilePath(String csvReportFilePath) {
        this.csvReportFilePath = csvReportFilePath;
        return this;
    }
}