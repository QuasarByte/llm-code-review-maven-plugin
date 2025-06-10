package com.quasarbyte.llm.codereview.maven.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quasarbyte.llm.codereview.maven.plugin.exception.LlmCodeReviewMavenPluginException;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.FileService;
import com.quasarbyte.llm.codereview.maven.plugin.service.PRulesFileReader;
import com.quasarbyte.llm.codereview.maven.plugin.service.ResourceLoader;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.FileServiceImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.PRulesFileReaderImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.PRulesJsonParserImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser.PRulesXmlParserImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser.ResourceLoaderImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesJsonParser;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesXmlParser;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.*;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.model.run.RunFailureConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.statistics.SeverityStatistics;
import com.quasarbyte.llm.codereview.sdk.service.*;
import com.quasarbyte.llm.codereview.sdk.service.impl.*;
import com.quasarbyte.llm.codereview.sdk.service.report.csv.CodeReviewReportCsvService;
import com.quasarbyte.llm.codereview.sdk.service.report.csv.impl.CodeReviewReportCsvServiceFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.report.html.CodeReviewReportHtmlService;
import com.quasarbyte.llm.codereview.sdk.service.report.html.impl.CodeReviewReportHtmlServiceFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.report.markdown.CodeReviewReportMarkdownService;
import com.quasarbyte.llm.codereview.sdk.service.report.markdown.impl.CodeReviewReportMarkdownServiceFactoryImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Mojo(name = "llm-code-review", defaultPhase = LifecyclePhase.VERIFY)
public class LlmCodeReviewMojo extends AbstractMojo {

    @Parameter(property = "reviewParameter")
    private PReviewParameter reviewParameter;

    @Parameter(property = "llmClientConfiguration")
    private PLlmClientConfiguration llmClientConfiguration;

    @Parameter(property = "parallelExecutionParameter")
    private PParallelExecutionParameter parallelExecutionParameter;

    @Parameter(property = "buildFailureConfiguration")
    private PBuildFailureConfiguration buildFailureConfiguration;

    @Parameter(property = "reportsConfiguration")
    private PReportsConfiguration reportsConfiguration;

    @Parameter(property = "persistenceConfiguration")
    private PPersistenceConfiguration persistenceConfiguration;

    @Parameter(property = "llmClientsConfiguration")
    private List<PLlmClientConfiguration> llmClientsConfiguration;

    private CodeReviewReportCsvService codeReviewReportCsvService;
    private CodeReviewReportHtmlService codeReviewReportHtmlService;
    private CodeReviewReportMarkdownService codeReviewReportMarkdownService;
    private FileService fileService;
    private LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository;
    private PFileGroupMapper fileGroupMapper;
    private PLlmClientConfigurationMapper clientConfigurationMapper;
    private PLlmQuotaMapper quotaMapper;
    private PProxyMapper proxyMapper;
    private PReviewParameterMapper reviewParameterMapper;
    private PReviewTargetMapper reviewTargetMapper;
    private PRhinoConfigurationMapper rhinoConfigurationMapper;
    private PRuleMapper ruleMapper;
    private PRulesFileReader rulesFileReader;
    private PRulesJsonParser rulesJsonParser;
    private PRulesXmlParser rulesXmlParser;
    private ParallelExecutionParameterMapper parallelExecutionParameterMapper;
    private PPersistenceConfigurationMapper persistenceConfigurationMapper;
    private PDataSourceConfigurationMapper dataSourceConfigurationMapper;
    private ResourceLoader resourceLoader;
    private ReviewParallelExecutionService reviewParallelExecutionService;
    private ReviewService reviewService;
    private RunFailureChecker runFailureChecker;
    private SeverityStatisticsCalculator severityStatisticsCalculator;

    public LlmCodeReviewMojo() {
        codeReviewReportMarkdownService = new CodeReviewReportMarkdownServiceFactoryImpl().create();
        codeReviewReportHtmlService = new CodeReviewReportHtmlServiceFactoryImpl().create();
        codeReviewReportCsvService = new CodeReviewReportCsvServiceFactoryImpl().create();
        rulesJsonParser = new PRulesJsonParserImpl();
        rulesXmlParser = new PRulesXmlParserImpl();
        fileService = new FileServiceImpl();
        resourceLoader = new ResourceLoaderImpl();
        rulesFileReader = new PRulesFileReaderImpl(fileService, rulesJsonParser, rulesXmlParser, resourceLoader);
        ruleMapper = new PRuleMapperImpl();
        fileGroupMapper = new PFileGroupMapperImpl(ruleMapper, rulesFileReader);
        reviewTargetMapper = new PReviewTargetMapperImpl(fileGroupMapper, ruleMapper, rulesFileReader);
        llmMessMapperRhinoConfigRepository = new LlmMessMapperRhinoConfigRepositoryFactoryImpl().create();
        rhinoConfigurationMapper = new PRhinoConfigurationMapperImpl(llmMessMapperRhinoConfigRepository, resourceLoader);
        quotaMapper = new PLlmQuotaMapperImpl();
        reviewParameterMapper = new PReviewParameterMapperImpl(quotaMapper, reviewTargetMapper, rhinoConfigurationMapper, ruleMapper, rulesFileReader);
        proxyMapper = new PProxyMapperImpl();
        clientConfigurationMapper = new PLlmClientConfigurationMapperImpl(proxyMapper);
        parallelExecutionParameterMapper = new ParallelExecutionParameterMapperImpl();
        dataSourceConfigurationMapper = new PDataSourceConfigurationMapperImpl();
        persistenceConfigurationMapper = new PPersistenceConfigurationMapperImpl(dataSourceConfigurationMapper);
        reviewService = new ReviewServiceFactoryImpl().create();
        reviewParallelExecutionService = new ReviewParallelExecutionServiceFactoryImpl().create();
        runFailureChecker = new RunFailureCheckerFactoryImpl().create();
        severityStatisticsCalculator = new SeverityStatisticsCalculatorImpl();
    }

    public PReviewParameter getReviewParameter() {
        return reviewParameter;
    }

    public LlmCodeReviewMojo setReviewParameter(PReviewParameter reviewParameter) {
        this.reviewParameter = reviewParameter;
        return this;
    }

    public PLlmClientConfiguration getLlmClientConfiguration() {
        return llmClientConfiguration;
    }

    public LlmCodeReviewMojo setLlmClientConfiguration(PLlmClientConfiguration llmClientConfiguration) {
        this.llmClientConfiguration = llmClientConfiguration;
        return this;
    }

    public PParallelExecutionParameter getParallelExecutionParameter() {
        return parallelExecutionParameter;
    }

    public LlmCodeReviewMojo setParallelExecutionParameter(PParallelExecutionParameter parallelExecutionParameter) {
        this.parallelExecutionParameter = parallelExecutionParameter;
        return this;
    }

    public PBuildFailureConfiguration getBuildFailureConfiguration() {
        return buildFailureConfiguration;
    }

    public LlmCodeReviewMojo setBuildFailureConfiguration(PBuildFailureConfiguration buildFailureConfiguration) {
        this.buildFailureConfiguration = buildFailureConfiguration;
        return this;
    }

    public PReportsConfiguration getReportsConfiguration() {
        return reportsConfiguration;
    }

    public LlmCodeReviewMojo setReportsConfiguration(PReportsConfiguration reportsConfiguration) {
        this.reportsConfiguration = reportsConfiguration;
        return this;
    }

    public PPersistenceConfiguration getPersistenceConfiguration() {
        return persistenceConfiguration;
    }

    public LlmCodeReviewMojo setPersistenceConfiguration(PPersistenceConfiguration persistenceConfiguration) {
        this.persistenceConfiguration = persistenceConfiguration;
        return this;
    }

    public List<PLlmClientConfiguration> getLlmClientsConfiguration() {
        return llmClientsConfiguration;
    }

    public LlmCodeReviewMojo setLlmClientsConfiguration(List<PLlmClientConfiguration> llmClientsConfiguration) {
        this.llmClientsConfiguration = llmClientsConfiguration;
        return this;
    }

    public CodeReviewReportCsvService getCodeReviewReportCsvService() {
        return codeReviewReportCsvService;
    }

    public LlmCodeReviewMojo setCodeReviewReportCsvService(CodeReviewReportCsvService codeReviewReportCsvService) {
        this.codeReviewReportCsvService = codeReviewReportCsvService;
        return this;
    }

    public CodeReviewReportHtmlService getCodeReviewReportHtmlService() {
        return codeReviewReportHtmlService;
    }

    public LlmCodeReviewMojo setCodeReviewReportHtmlService(CodeReviewReportHtmlService codeReviewReportHtmlService) {
        this.codeReviewReportHtmlService = codeReviewReportHtmlService;
        return this;
    }

    public CodeReviewReportMarkdownService getCodeReviewReportMarkdownService() {
        return codeReviewReportMarkdownService;
    }

    public LlmCodeReviewMojo setCodeReviewReportMarkdownService(CodeReviewReportMarkdownService codeReviewReportMarkdownService) {
        this.codeReviewReportMarkdownService = codeReviewReportMarkdownService;
        return this;
    }

    public FileService getFileService() {
        return fileService;
    }

    public LlmCodeReviewMojo setFileService(FileService fileService) {
        this.fileService = fileService;
        return this;
    }

    public LlmMessMapperRhinoConfigRepository getLlmMessMapperRhinoConfigRepository() {
        return llmMessMapperRhinoConfigRepository;
    }

    public LlmCodeReviewMojo setLlmMessMapperRhinoConfigRepository(LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository) {
        this.llmMessMapperRhinoConfigRepository = llmMessMapperRhinoConfigRepository;
        return this;
    }

    public PFileGroupMapper getFileGroupMapper() {
        return fileGroupMapper;
    }

    public LlmCodeReviewMojo setFileGroupMapper(PFileGroupMapper fileGroupMapper) {
        this.fileGroupMapper = fileGroupMapper;
        return this;
    }

    public PLlmClientConfigurationMapper getClientConfigurationMapper() {
        return clientConfigurationMapper;
    }

    public LlmCodeReviewMojo setClientConfigurationMapper(PLlmClientConfigurationMapper clientConfigurationMapper) {
        this.clientConfigurationMapper = clientConfigurationMapper;
        return this;
    }

    public PLlmQuotaMapper getQuotaMapper() {
        return quotaMapper;
    }

    public LlmCodeReviewMojo setQuotaMapper(PLlmQuotaMapper quotaMapper) {
        this.quotaMapper = quotaMapper;
        return this;
    }

    public PProxyMapper getProxyMapper() {
        return proxyMapper;
    }

    public LlmCodeReviewMojo setProxyMapper(PProxyMapper proxyMapper) {
        this.proxyMapper = proxyMapper;
        return this;
    }

    public PReviewParameterMapper getReviewParameterMapper() {
        return reviewParameterMapper;
    }

    public LlmCodeReviewMojo setReviewParameterMapper(PReviewParameterMapper reviewParameterMapper) {
        this.reviewParameterMapper = reviewParameterMapper;
        return this;
    }

    public PReviewTargetMapper getReviewTargetMapper() {
        return reviewTargetMapper;
    }

    public LlmCodeReviewMojo setReviewTargetMapper(PReviewTargetMapper reviewTargetMapper) {
        this.reviewTargetMapper = reviewTargetMapper;
        return this;
    }

    public PRhinoConfigurationMapper getRhinoConfigurationMapper() {
        return rhinoConfigurationMapper;
    }

    public LlmCodeReviewMojo setRhinoConfigurationMapper(PRhinoConfigurationMapper rhinoConfigurationMapper) {
        this.rhinoConfigurationMapper = rhinoConfigurationMapper;
        return this;
    }

    public PRuleMapper getRuleMapper() {
        return ruleMapper;
    }

    public LlmCodeReviewMojo setRuleMapper(PRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
        return this;
    }

    public PRulesFileReader getRulesFileReader() {
        return rulesFileReader;
    }

    public LlmCodeReviewMojo setRulesFileReader(PRulesFileReader rulesFileReader) {
        this.rulesFileReader = rulesFileReader;
        return this;
    }

    public PRulesJsonParser getRulesJsonParser() {
        return rulesJsonParser;
    }

    public LlmCodeReviewMojo setRulesJsonParser(PRulesJsonParser rulesJsonParser) {
        this.rulesJsonParser = rulesJsonParser;
        return this;
    }

    public PRulesXmlParser getRulesXmlParser() {
        return rulesXmlParser;
    }

    public LlmCodeReviewMojo setRulesXmlParser(PRulesXmlParser rulesXmlParser) {
        this.rulesXmlParser = rulesXmlParser;
        return this;
    }

    public ParallelExecutionParameterMapper getParallelExecutionParameterMapper() {
        return parallelExecutionParameterMapper;
    }

    public LlmCodeReviewMojo setParallelExecutionParameterMapper(ParallelExecutionParameterMapper parallelExecutionParameterMapper) {
        this.parallelExecutionParameterMapper = parallelExecutionParameterMapper;
        return this;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public LlmCodeReviewMojo setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        return this;
    }

    public ReviewParallelExecutionService getReviewParallelExecutionService() {
        return reviewParallelExecutionService;
    }

    public LlmCodeReviewMojo setReviewParallelExecutionService(ReviewParallelExecutionService reviewParallelExecutionService) {
        this.reviewParallelExecutionService = reviewParallelExecutionService;
        return this;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }

    public LlmCodeReviewMojo setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
        return this;
    }

    public RunFailureChecker getRunFailureChecker() {
        return runFailureChecker;
    }

    public LlmCodeReviewMojo setRunFailureChecker(RunFailureChecker runFailureChecker) {
        this.runFailureChecker = runFailureChecker;
        return this;
    }

    public SeverityStatisticsCalculator getSeverityStatisticsCalculator() {
        return severityStatisticsCalculator;
    }

    public LlmCodeReviewMojo setSeverityStatisticsCalculator(SeverityStatisticsCalculator severityStatisticsCalculator) {
        this.severityStatisticsCalculator = severityStatisticsCalculator;
        return this;
    }

    public PPersistenceConfigurationMapper getPersistenceConfigurationMapper() {
        return persistenceConfigurationMapper;
    }

    public LlmCodeReviewMojo setPersistenceConfigurationMapper(PPersistenceConfigurationMapper persistenceConfigurationMapper) {
        this.persistenceConfigurationMapper = persistenceConfigurationMapper;
        return this;
    }

    public PDataSourceConfigurationMapper getDataSourceConfigurationMapper() {
        return dataSourceConfigurationMapper;
    }

    public LlmCodeReviewMojo setDataSourceConfigurationMapper(PDataSourceConfigurationMapper dataSourceConfigurationMapper) {
        this.dataSourceConfigurationMapper = dataSourceConfigurationMapper;
        return this;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Apply default values matching CLI behavior
        PParallelExecutionParameter effectiveParallelExecutionParameter = parallelExecutionParameter != null ?
                parallelExecutionParameter :
                new PParallelExecutionParameter().setBatchSize(null).setPoolSize(null);

        PBuildFailureConfiguration effectiveBuildFailureConfiguration = buildFailureConfiguration != null ?
                buildFailureConfiguration :
                new PBuildFailureConfiguration().setCriticalThreshold(1).setWarningThreshold(100);

        String reviewParameterAsJson;
        String llmClientConfigurationAsJson;
        String llmClientsConfigurationAsJson;
        String parallelExecutionParameterAsJson;
        String buildFailureConfigurationAsJson;

        try {
            reviewParameterAsJson = objectMapper.writeValueAsString(reviewParameter);
            llmClientConfigurationAsJson = objectMapper.writeValueAsString(maskedLlmClientConfigurationCopy(llmClientConfiguration));

            if (llmClientsConfiguration != null && !llmClientsConfiguration.isEmpty()) {
                llmClientsConfigurationAsJson = objectMapper.writeValueAsString(maskedLlmClientConfigurationCopy(llmClientsConfiguration));
            } else {
                llmClientsConfigurationAsJson = null;
            }

            parallelExecutionParameterAsJson = objectMapper.writeValueAsString(effectiveParallelExecutionParameter);
            buildFailureConfigurationAsJson = objectMapper.writeValueAsString(effectiveBuildFailureConfiguration);
        } catch (JsonProcessingException e) {
            getLog().error("Cannot serialize Maven plugin configuration to JSON: " + e.getMessage(), e);
            throw new MojoExecutionException(String.format("Cannot serialize maven plugin configuration to JSON, error message: '%s'", e.getMessage()), e);
        }

        getLog().info("Starting LLM Code Review plugin execution...");

        // Validation logic for LLM client configurations
        if (llmClientConfiguration != null && llmClientsConfiguration != null && !llmClientsConfiguration.isEmpty()) {
            getLog().error("Provide either a single LLM Client configuration or a list, not both.");
            throw new ValidationException("Provide either a single LLM Client configuration or a list, not both.");
        } else if (llmClientConfiguration == null && (llmClientsConfiguration == null || llmClientsConfiguration.isEmpty())) {
            getLog().error("LLM Client configuration is not provided.");
            throw new ValidationException("LLM Client configuration is not provided.");
        }

        getLog().debug("reviewParameter body:");
        getLog().debug(reviewParameterAsJson);

        getLog().debug("llmClientConfiguration body:");
        getLog().debug(llmClientConfigurationAsJson);

        getLog().debug("llmClientsConfiguration body:");
        getLog().debug(llmClientsConfigurationAsJson != null ? llmClientsConfigurationAsJson : "null");

        getLog().debug("parallelExecutionParameter body:");
        getLog().debug(parallelExecutionParameterAsJson);

        getLog().debug("buildFailureConfiguration body:");
        getLog().debug(buildFailureConfigurationAsJson);

        final ReviewParameter mappedRP;
        try {
            mappedRP = this.reviewParameterMapper.map(reviewParameter);
            getLog().info("Mapped reviewParameter successfully.");
        } catch (Exception e) {
            getLog().error("Failed to map reviewParameter: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed to map reviewParameter: " + e.getMessage(), e);
        }

        final PersistenceConfiguration mappedPC;
        try {
            if (persistenceConfiguration != null) {
                mappedPC = persistenceConfigurationMapper.map(persistenceConfiguration);
                getLog().info("Mapped persistenceConfiguration successfully.");
            } else {
                mappedPC = null;
                getLog().info("No persistence configuration provided - using null.");
            }
        } catch (Exception e) {
            getLog().error("Failed to map persistenceConfiguration: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed to map persistenceConfiguration: " + e.getMessage(), e);
        }

        LlmClientFactory llmClientFactory = new LlmClientFactoryImpl();

        final Optional<LlmClient> llmClient;
        final List<LlmClient> llmClients;

        if (llmClientConfiguration != null) {
            // Single client path
            LlmClientConfiguration clientConfiguration;
            try {
                clientConfiguration = clientConfigurationMapper.map(llmClientConfiguration);
                getLog().info("Mapped single llmClientConfiguration successfully.");
            } catch (Exception e) {
                getLog().error("Failed to map llmClientConfiguration: " + e.getMessage(), e);
                throw new MojoExecutionException("Failed to map llmClientConfiguration: " + e.getMessage(), e);
            }

            llmClient = Optional.of(llmClientFactory.create(clientConfiguration));
            llmClients = Collections.emptyList();
            getLog().info("Created single LLM client");
        } else {
            // Multiple clients path
            List<LlmClientConfiguration> clientConfigurations;
            try {
                clientConfigurations = clientConfigurationMapper.map(llmClientsConfiguration);
                getLog().info("Mapped " + llmClientsConfiguration.size() + " llmClientsConfiguration successfully.");
            } catch (Exception e) {
                getLog().error("Failed to map llmClientsConfiguration: " + e.getMessage(), e);
                throw new MojoExecutionException("Failed to map llmClientsConfiguration: " + e.getMessage(), e);
            }

            llmClient = Optional.empty();
            llmClients = llmClientFactory.create(clientConfigurations);
            getLog().info("Created " + llmClients.size() + " LLM clients");
        }

        final ReviewResult result;

        try {
            if (effectiveParallelExecutionParameter == null || (effectiveParallelExecutionParameter.getBatchSize() == null && effectiveParallelExecutionParameter.getPoolSize() == null)) {
                getLog().info("Executing review in single-threaded mode.");

                if (llmClient.isPresent()) {
                    result = reviewService.review(mappedRP, llmClient.get(), mappedPC);
                } else {
                    if (llmClients.size() == 1) {
                        result = reviewService.review(mappedRP, llmClients.get(0), mappedPC);
                    } else {
                        throw new ValidationException("More than one or zero LlmClients is present.");
                    }
                }
            } else {
                if (effectiveParallelExecutionParameter.getBatchSize() == null) {
                    getLog().error("parallel execution parameter batch size is null");
                    throw new ValidationException("parallel execution parameter batch size is null");
                }
                if (effectiveParallelExecutionParameter.getPoolSize() == null) {
                    getLog().error("parallel execution parameter pool size is null");
                    throw new ValidationException("parallel execution parameter pool size is null");
                }
                ParallelExecutionParameter executionParameter = parallelExecutionParameterMapper.map(effectiveParallelExecutionParameter);
                getLog().info(String.format("Executing review in parallel mode. Batch size: %d, Pool size: %d", executionParameter.getBatchSize(), effectiveParallelExecutionParameter.getPoolSize()));

                if (llmClient.isPresent()) {
                    result = reviewParallelExecutionService.review(mappedRP, llmClient.get(), mappedPC, executionParameter);
                } else {
                    result = reviewParallelExecutionService.review(mappedRP, llmClients, mappedPC, executionParameter);
                }
            }
        } catch (Exception e) {
            getLog().error("Failed during review execution: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed during review execution: " + e.getMessage(), e);
        }

        String resultAsJson;
        try {
            resultAsJson = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            getLog().error("Cannot serialize review result to JSON: " + e.getMessage(), e);
            throw new MojoExecutionException(String.format("Cannot serialize review result to JSON, error message: '%s'", e.getMessage()), e);
        }

        getLog().info("Review result items size: " + result.getItems().size());
        getLog().debug("Review result body:");
        getLog().debug(resultAsJson);

        SeverityStatistics severityStatistics;
        try {
            severityStatistics = severityStatisticsCalculator.calculate(result);
            getLog().info(String.format("Calculated severity statistics: InfoCount=%d, WarningCount=%d, CriticalCount=%d",
                    severityStatistics.getInfoCount(),
                    severityStatistics.getWarningCount(),
                    severityStatistics.getCriticalCount()));
        } catch (Exception e) {
            getLog().error("Failed to calculate severity statistics: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed to calculate severity statistics: " + e.getMessage(), e);
        }

        getLog().info("Review result body:");
        getLog().info(resultAsJson);

        createReports(result, resultAsJson);

        final boolean failBuild;
        try {
            failBuild = runFailureChecker.check(new RunFailureConfiguration()
                            .setWarningThreshold(effectiveBuildFailureConfiguration.getWarningThreshold())
                            .setCriticalThreshold(effectiveBuildFailureConfiguration.getCriticalThreshold()),
                    severityStatistics);

            if (failBuild) {
                getLog().warn("Build failure criteria met. Failing build.");
            } else {
                getLog().info("Build passed failure check.");
            }
        } catch (Exception e) {
            getLog().error("Failed during build failure check: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed during build failure check: " + e.getMessage(), e);
        }

        if (failBuild) {
            throw new MojoFailureException(String.format("Build failed. Failure check did not pass. InfoCount: %d, WarningCount: %d, CriticalCount: %d",
                    severityStatistics.getInfoCount(),
                    severityStatistics.getWarningCount(),
                    severityStatistics.getCriticalCount()));
        }

        getLog().info("LLM Code Review plugin execution finished.");
    }

    private void createReports(ReviewResult result, String resultAsJson) {
        if (reportsConfiguration != null && reportsConfiguration.getJsonReportFilePath() != null && !reportsConfiguration.getJsonReportFilePath().trim().isEmpty()) {
            createFile(resultAsJson.getBytes(StandardCharsets.UTF_8), reportsConfiguration.getJsonReportFilePath());
        }

        if (reportsConfiguration != null && reportsConfiguration.getMarkdownReportFilePath() != null && !reportsConfiguration.getMarkdownReportFilePath().trim().isEmpty()) {
            String reportBody = codeReviewReportMarkdownService.generateMarkdownReport(result);
            createFile(reportBody.getBytes(StandardCharsets.UTF_8), reportsConfiguration.getMarkdownReportFilePath());
        }

        if (reportsConfiguration != null && reportsConfiguration.getHtmlReportFilePath() != null && !reportsConfiguration.getHtmlReportFilePath().trim().isEmpty()) {
            String reportBody = codeReviewReportHtmlService.generateHtmlReport(result);
            createFile(reportBody.getBytes(StandardCharsets.UTF_8), reportsConfiguration.getHtmlReportFilePath());
        }

        if (reportsConfiguration != null && reportsConfiguration.getCsvReportFilePath() != null && !reportsConfiguration.getCsvReportFilePath().trim().isEmpty()) {
            String reportBody = codeReviewReportCsvService.generateCsvReport(result);
            createFile(reportBody.getBytes(StandardCharsets.UTF_8), reportsConfiguration.getCsvReportFilePath());
        }
    }

    private void createFile(byte[] bytes, String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new ValidationException("FilePath cannot be null or empty.");
        }
        if (isStdOut(filePath)) {
            System.out.println(new String(bytes));
        } else if (isStdErr(filePath)) {
            System.err.println(new String(bytes));
        } else {
            try {
                Files.createDirectories(Paths.get(filePath).getParent());
                Files.write(Paths.get(filePath), bytes, StandardOpenOption.CREATE);
            } catch (IOException e) {
                getLog().error("Failed to write result file: " + e.getMessage(), e);
                throw new LlmCodeReviewMavenPluginException("Failed to write result file: " + e.getMessage(), e);
            }
        }
    }

    private static boolean isStdOut(String filePath) {
        return "stdout".equalsIgnoreCase(filePath);
    }

    private static boolean isStdErr(String filePath) {
        return "stderr".equalsIgnoreCase(filePath);
    }

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }

    private static PLlmClientConfiguration maskedLlmClientConfigurationCopy(PLlmClientConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        PLlmClientConfiguration masked = new PLlmClientConfiguration();
        masked.setCheckJacksonVersionCompatibility(configuration.getCheckJacksonVersionCompatibility())
                .setResponseValidation(configuration.getResponseValidation())
                .setTimeoutDuration(configuration.getTimeoutDuration())
                .setMaxRetries(configuration.getMaxRetries())
                .setHeadersMap(configuration.getHeadersMap())
                .setQueryParamsMap(configuration.getQueryParamsMap())
                .setProxy(configuration.getProxy())
                .setApiKey(configuration.getApiKey() == null ? null : "******")
                .setAzureServiceVersion(configuration.getAzureServiceVersion())
                .setBaseUrl(configuration.getBaseUrl())
                .setOrganization(configuration.getOrganization())
                .setProject(configuration.getProject());
        return masked;
    }

    private static List<PLlmClientConfiguration> maskedLlmClientConfigurationCopy(List<PLlmClientConfiguration> configurations) {
        if (configurations == null) {
            return null;
        }
        return configurations.stream()
                .map(LlmCodeReviewMojo::maskedLlmClientConfigurationCopy)
                .collect(java.util.stream.Collectors.toList());
    }

}
