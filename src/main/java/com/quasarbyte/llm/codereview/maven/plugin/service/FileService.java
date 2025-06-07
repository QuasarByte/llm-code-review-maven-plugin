package com.quasarbyte.llm.codereview.maven.plugin.service;

import java.util.Optional;

public interface FileService {
    Optional<String> getFileExtension(String fileName);
}
