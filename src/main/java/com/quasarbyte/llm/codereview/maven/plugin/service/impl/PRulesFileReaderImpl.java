package com.quasarbyte.llm.codereview.maven.plugin.service.impl;

import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.FileTypeEnum;
import com.quasarbyte.llm.codereview.maven.plugin.model.PRule;
import com.quasarbyte.llm.codereview.maven.plugin.service.FileService;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.ResourceLoader;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesJsonParser;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class PRulesFileReaderImpl implements PRulesFileReader {

    private static final Logger logger = LoggerFactory.getLogger(PRulesFileReaderImpl.class);

    private final FileService fileService;
    private final PRulesJsonParser rulesJsonParser;
    private final PRulesXmlParser rulesXmlParser;
    private final ResourceLoader resourceLoader;

    public PRulesFileReaderImpl(FileService fileService,
                                PRulesJsonParser rulesJsonParser,
                                PRulesXmlParser rulesXmlParser,
                                ResourceLoader resourceLoader) {
        this.fileService = fileService;
        this.rulesJsonParser = rulesJsonParser;
        this.rulesXmlParser = rulesXmlParser;
        this.resourceLoader = resourceLoader;
        logger.debug("PRulesFileReaderImpl initialized.");
    }

    @Override
    public List<PRule> readPRules(String filePath) throws Exception {
        logger.info("Reading PRules from file: '{}'", filePath);

        if (notNullOrBlank(filePath)) {

            String fileExtension = fileService.getFileExtension(filePath)
                    .orElseThrow(() -> {
                        logger.error("Cannot determine file extension for '{}'", filePath);
                        return new LlmCodeReviewMavenPluginException(String.format("Can not determine file extension of file: '%s'", filePath));
                    });

            logger.debug("Detected file extension '{}' for '{}'", fileExtension, filePath);

            Optional<FileTypeEnum> fileTypeEnumOptional = FileTypeEnum.findByExtension(fileExtension);

            if (fileTypeEnumOptional.isPresent()) {
                logger.debug("File type '{}' identified for extension '{}'", fileTypeEnumOptional.get(), fileExtension);

                if (FileTypeEnum.JSON.equals(fileTypeEnumOptional.get())) {
                    logger.info("Parsing JSON rules from '{}'", filePath);
                    String body = resourceLoader.load(filePath);
                    List<PRule> rules = rulesJsonParser.parseRules(body);
                    logger.info("Parsed {} rules from JSON file '{}'", rules.size(), filePath);
                    return rules;

                } else if (FileTypeEnum.XML.equals(fileTypeEnumOptional.get())) {
                    logger.info("Parsing XML rules from '{}'", filePath);
                    String body = resourceLoader.load(filePath);
                    List<PRule> rules = rulesXmlParser.parseRules(body);
                    logger.info("Parsed {} rules from XML file '{}'", rules.size(), filePath);
                    return rules;

                } else {
                    logger.error("Unsupported file type: '{}'", fileExtension);
                    throw new ValidationException(String.format("Unsupported file type: '%s'. Supported files types only JSON and XML.", fileExtension));
                }

            } else {
                logger.error("Unsupported file type: '{}'", fileExtension);
                throw new ValidationException(String.format("Unsupported file type: '%s'. Supported files types only JSON and XML.", fileExtension));
            }
        } else {
            logger.error("The file path is empty");
            throw new ValidationException("The file path is empty");
        }
    }

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
}
