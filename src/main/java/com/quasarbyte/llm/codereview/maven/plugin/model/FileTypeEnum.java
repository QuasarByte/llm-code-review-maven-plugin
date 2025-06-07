package com.quasarbyte.llm.codereview.maven.plugin.model;

import java.util.Optional;

/**
 * Enumeration of supported file types for rules definition.
 * <p>
 * This enum is used to specify the file format (JSON or XML)
 * in which rules for code review are defined.
 * </p>
 */
public enum FileTypeEnum {
    /**
     * Represents files in JSON (JavaScript Object Notation) format.
     */
    JSON,
    /**
     * Represents files in XML (Extensible Markup Language) format.
     */
    XML;

    public static Optional<FileTypeEnum> findByExtension(String extension) {
        if ("json".equalsIgnoreCase(extension)) {
            return Optional.of(JSON);
        } else if ("xml".equalsIgnoreCase(extension)) {
            return Optional.of(XML);
        } else {
            return Optional.empty();
        }
    }

}
