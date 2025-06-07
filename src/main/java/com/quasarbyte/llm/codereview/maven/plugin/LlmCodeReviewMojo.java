package com.quasarbyte.llm.codereview.maven.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.ResourceLoader;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser.PRulesXmlParserImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.parser.ResourceLoaderImpl;
import com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper.*;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesJsonParser;
import com.quasarbyte.llm.codereview.maven.plugin.service.parser.PRulesXmlParser;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.*;
import com.quasarbyte.llm.codereview.sdk.model.configuration.LlmClientConfiguration;
import com.quasarbyte.llm.codereview.sdk.model.parameter.LlmClient;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ReviewParameter;
import com.quasarbyte.llm.codereview.sdk.model.review.ReviewResult;
import com.quasarbyte.llm.codereview.sdk.service.*;
import com.quasarbyte.llm.codereview.sdk.service.impl.LlmClientFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.impl.LlmMessMapperRhinoConfigRepositoryFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.impl.ReviewParallelExecutionServiceFactoryImpl;
import com.quasarbyte.llm.codereview.sdk.service.impl.ReviewServiceFactoryImpl;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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

    private PRulesJsonParser rulesJsonParser;
    private PRulesXmlParser rulesXmlParser;

    private FileService fileService;
    private ResourceLoader resourceLoader;
    private PRulesFileReader rulesFileReader;

    private PRuleMapper ruleMapper;
    private PFileGroupMapper fileGroupMapper;
    private PReviewTargetMapper reviewTargetMapper;

    private LlmMessMapperRhinoConfigRepositoryFactory rhinoConfigRepositoryFactory;
    private LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository;

    private PRhinoConfigurationMapper rhinoConfigurationMapper;
    private PLlmQuotaMapper quotaMapper;
    private PReviewParameterMapper reviewParameterMapper;
    private PProxyMapper proxyMapper;
    private PLlmClientConfigurationMapper clientConfigurationMapper;
    private ParallelExecutionParameterMapper parallelExecutionParameterMapper;

    private ReviewServiceFactory reviewServiceFactory;
    private ReviewService reviewService;

    private ReviewParallelExecutionServiceFactory reviewParallelExecutionServiceFactory;
    private ReviewParallelExecutionService reviewParallelExecutionService;

    private BuildFailureChecker buildFailureChecker;
    private SeverityStatisticsCalculator severityStatisticsCalculator;

    public LlmCodeReviewMojo() {
        rulesJsonParser = new PRulesJsonParserImpl();
        rulesXmlParser = new PRulesXmlParserImpl();

        fileService = new FileServiceImpl();
        resourceLoader = new ResourceLoaderImpl();
        rulesFileReader = new PRulesFileReaderImpl(fileService, rulesJsonParser, rulesXmlParser, resourceLoader);

        ruleMapper = new PRuleMapperImpl();
        fileGroupMapper = new PFileGroupMapperImpl(ruleMapper, rulesFileReader);
        reviewTargetMapper = new PReviewTargetMapperImpl(fileGroupMapper, ruleMapper, rulesFileReader);

        rhinoConfigRepositoryFactory = new LlmMessMapperRhinoConfigRepositoryFactoryImpl();
        llmMessMapperRhinoConfigRepository = rhinoConfigRepositoryFactory.create();

        rhinoConfigurationMapper = new PRhinoConfigurationMapperImpl(llmMessMapperRhinoConfigRepository, resourceLoader);

        quotaMapper = new PLlmQuotaMapperImpl();
        reviewParameterMapper = new PReviewParameterMapperImpl(quotaMapper, reviewTargetMapper, rhinoConfigurationMapper, ruleMapper, rulesFileReader);
        proxyMapper = new PProxyMapperImpl();
        clientConfigurationMapper = new PLlmClientConfigurationMapperImpl(proxyMapper);
        parallelExecutionParameterMapper = new ParallelExecutionParameterMapperImpl();

        reviewServiceFactory = new ReviewServiceFactoryImpl();
        reviewService = reviewServiceFactory.create();

        reviewParallelExecutionServiceFactory = new ReviewParallelExecutionServiceFactoryImpl();
        reviewParallelExecutionService = reviewParallelExecutionServiceFactory.create();

        buildFailureChecker = new BuildFailureCheckerImpl();
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

    public FileService getFileService() {
        return fileService;
    }

    public LlmCodeReviewMojo setFileService(FileService fileService) {
        this.fileService = fileService;
        return this;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public LlmCodeReviewMojo setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        return this;
    }

    public PRulesFileReader getRulesFileReader() {
        return rulesFileReader;
    }

    public LlmCodeReviewMojo setRulesFileReader(PRulesFileReader rulesFileReader) {
        this.rulesFileReader = rulesFileReader;
        return this;
    }

    public PRuleMapper getRuleMapper() {
        return ruleMapper;
    }

    public LlmCodeReviewMojo setRuleMapper(PRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
        return this;
    }

    public PFileGroupMapper getFileGroupMapper() {
        return fileGroupMapper;
    }

    public LlmCodeReviewMojo setFileGroupMapper(PFileGroupMapper fileGroupMapper) {
        this.fileGroupMapper = fileGroupMapper;
        return this;
    }

    public PReviewTargetMapper getReviewTargetMapper() {
        return reviewTargetMapper;
    }

    public LlmCodeReviewMojo setReviewTargetMapper(PReviewTargetMapper reviewTargetMapper) {
        this.reviewTargetMapper = reviewTargetMapper;
        return this;
    }

    public LlmMessMapperRhinoConfigRepositoryFactory getRhinoConfigRepositoryFactory() {
        return rhinoConfigRepositoryFactory;
    }

    public LlmCodeReviewMojo setRhinoConfigRepositoryFactory(LlmMessMapperRhinoConfigRepositoryFactory rhinoConfigRepositoryFactory) {
        this.rhinoConfigRepositoryFactory = rhinoConfigRepositoryFactory;
        return this;
    }

    public LlmMessMapperRhinoConfigRepository getLlmMessMapperRhinoConfigRepository() {
        return llmMessMapperRhinoConfigRepository;
    }

    public LlmCodeReviewMojo setLlmMessMapperRhinoConfigRepository(LlmMessMapperRhinoConfigRepository llmMessMapperRhinoConfigRepository) {
        this.llmMessMapperRhinoConfigRepository = llmMessMapperRhinoConfigRepository;
        return this;
    }

    public PRhinoConfigurationMapper getRhinoConfigurationMapper() {
        return rhinoConfigurationMapper;
    }

    public LlmCodeReviewMojo setRhinoConfigurationMapper(PRhinoConfigurationMapper rhinoConfigurationMapper) {
        this.rhinoConfigurationMapper = rhinoConfigurationMapper;
        return this;
    }

    public PLlmQuotaMapper getQuotaMapper() {
        return quotaMapper;
    }

    public LlmCodeReviewMojo setQuotaMapper(PLlmQuotaMapper quotaMapper) {
        this.quotaMapper = quotaMapper;
        return this;
    }

    public PReviewParameterMapper getReviewParameterMapper() {
        return reviewParameterMapper;
    }

    public LlmCodeReviewMojo setReviewParameterMapper(PReviewParameterMapper reviewParameterMapper) {
        this.reviewParameterMapper = reviewParameterMapper;
        return this;
    }

    public PProxyMapper getProxyMapper() {
        return proxyMapper;
    }

    public LlmCodeReviewMojo setProxyMapper(PProxyMapper proxyMapper) {
        this.proxyMapper = proxyMapper;
        return this;
    }

    public PLlmClientConfigurationMapper getClientConfigurationMapper() {
        return clientConfigurationMapper;
    }

    public LlmCodeReviewMojo setClientConfigurationMapper(PLlmClientConfigurationMapper clientConfigurationMapper) {
        this.clientConfigurationMapper = clientConfigurationMapper;
        return this;
    }

    public ParallelExecutionParameterMapper getParallelExecutionParameterMapper() {
        return parallelExecutionParameterMapper;
    }

    public LlmCodeReviewMojo setParallelExecutionParameterMapper(ParallelExecutionParameterMapper parallelExecutionParameterMapper) {
        this.parallelExecutionParameterMapper = parallelExecutionParameterMapper;
        return this;
    }

    public ReviewServiceFactory getReviewServiceFactory() {
        return reviewServiceFactory;
    }

    public LlmCodeReviewMojo setReviewServiceFactory(ReviewServiceFactory reviewServiceFactory) {
        this.reviewServiceFactory = reviewServiceFactory;
        return this;
    }

    public ReviewService getReviewService() {
        return reviewService;
    }

    public LlmCodeReviewMojo setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
        return this;
    }

    public ReviewParallelExecutionServiceFactory getReviewParallelExecutionServiceFactory() {
        return reviewParallelExecutionServiceFactory;
    }

    public LlmCodeReviewMojo setReviewParallelExecutionServiceFactory(ReviewParallelExecutionServiceFactory reviewParallelExecutionServiceFactory) {
        this.reviewParallelExecutionServiceFactory = reviewParallelExecutionServiceFactory;
        return this;
    }

    public ReviewParallelExecutionService getReviewParallelExecutionService() {
        return reviewParallelExecutionService;
    }

    public LlmCodeReviewMojo setReviewParallelExecutionService(ReviewParallelExecutionService reviewParallelExecutionService) {
        this.reviewParallelExecutionService = reviewParallelExecutionService;
        return this;
    }

    public BuildFailureChecker getBuildFailureChecker() {
        return buildFailureChecker;
    }

    public LlmCodeReviewMojo setBuildFailureChecker(BuildFailureChecker buildFailureChecker) {
        this.buildFailureChecker = buildFailureChecker;
        return this;
    }

    public SeverityStatisticsCalculator getSeverityStatisticsCalculator() {
        return severityStatisticsCalculator;
    }

    public LlmCodeReviewMojo setSeverityStatisticsCalculator(SeverityStatisticsCalculator severityStatisticsCalculator) {
        this.severityStatisticsCalculator = severityStatisticsCalculator;
        return this;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        String reviewParameterAsJson;
        String llmClientConfigurationAsJson;
        String parallelExecutionParameterAsJson;
        String buildFailureConfigurationAsJson;

        try {
            reviewParameterAsJson = objectMapper.writeValueAsString(reviewParameter);
            llmClientConfigurationAsJson = objectMapper.writeValueAsString(maskedLlmClientConfigurationCopy(llmClientConfiguration));
            parallelExecutionParameterAsJson = objectMapper.writeValueAsString(parallelExecutionParameter);
            buildFailureConfigurationAsJson = objectMapper.writeValueAsString(buildFailureConfiguration);
        } catch (JsonProcessingException e) {
            getLog().error("Cannot serialize Maven plugin configuration to JSON: " + e.getMessage(), e);
            throw new MojoExecutionException(String.format("Cannot serialize maven plugin configuration to JSON, error message: '%s'", e.getMessage()), e);
        }

        getLog().info("Starting LLM Code Review plugin execution...");
        getLog().debug("reviewParameter body:");
        getLog().debug(reviewParameterAsJson);

        getLog().debug("llmClientConfiguration body:");
        getLog().debug(llmClientConfigurationAsJson);

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

        LlmClientFactory llmClientFactory = new LlmClientFactoryImpl();
        LlmClientConfiguration clientConfiguration;
        try {
            clientConfiguration = clientConfigurationMapper.map(llmClientConfiguration);
            getLog().info("Mapped llmClientConfiguration successfully.");
        } catch (Exception e) {
            getLog().error("Failed to map llmClientConfiguration: " + e.getMessage(), e);
            throw new MojoExecutionException("Failed to map llmClientConfiguration: " + e.getMessage(), e);
        }

        LlmClient llmClient = llmClientFactory.create(clientConfiguration);

        final ReviewResult result;

        try {
            if (parallelExecutionParameter == null || (parallelExecutionParameter.getBatchSize() == null && parallelExecutionParameter.getPoolSize() == null)) {
                getLog().info("Executing review in single-threaded mode.");
                result = reviewService.review(mappedRP, llmClient);
            } else {
                if (parallelExecutionParameter.getBatchSize() == null) {
                    getLog().error("parallel execution parameter batch size is null");
                    throw new ValidationException("parallel execution parameter batch size is null");
                }
                if (parallelExecutionParameter.getPoolSize() == null) {
                    getLog().error("parallel execution parameter pool size is null");
                    throw new ValidationException("parallel execution parameter pool size is null");
                }
                ParallelExecutionParameter executionParameter = parallelExecutionParameterMapper.map(parallelExecutionParameter);
                getLog().info(String.format("Executing review in parallel mode. Batch size: %d, Pool size: %d", executionParameter.getBatchSize(), parallelExecutionParameter.getPoolSize()));
                result = reviewParallelExecutionService.review(mappedRP, llmClient, executionParameter);
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

        boolean failBuild = false;
        try {
            failBuild = buildFailureChecker.check(buildFailureConfiguration, severityStatistics);
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

    private static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }

    private static PLlmClientConfiguration maskedLlmClientConfigurationCopy(PLlmClientConfiguration configuration) {
        PLlmClientConfiguration masked = new PLlmClientConfiguration();
        masked.setCheckJacksonVersionCompatibility(configuration.getCheckJacksonVersionCompatibility())
                .setResponseValidation(configuration.getResponseValidation())
                .setTimeoutDuration(configuration.getTimeoutDuration())
                .setMaxRetries(configuration.getMaxRetries())
                .setJsonMapper(configuration.getJsonMapper())
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

}
