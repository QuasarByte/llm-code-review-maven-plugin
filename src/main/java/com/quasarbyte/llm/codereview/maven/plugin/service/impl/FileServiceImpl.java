package com.quasarbyte.llm.codereview.maven.plugin.service.impl;

import com.quasarbyte.llm.codereview.maven.plugin.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public Optional<String> getFileExtension(String fileName) {
        if (fileName == null) {
            logger.warn("getFileExtension called with null fileName.");
            return Optional.empty();
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            logger.info("No extension found in fileName '{}'.", fileName);
            return Optional.empty(); // No extension
        }
        if (dotIndex == fileName.length() - 1) {
            logger.info("FileName '{}' ends with a dot, no extension found.", fileName);
            return Optional.empty(); // Ends with a dot
        }
        String extension = fileName.substring(dotIndex + 1);
        logger.debug("Extracted extension '{}' from fileName '{}'.", extension, fileName);
        return Optional.of(extension);
    }
}
