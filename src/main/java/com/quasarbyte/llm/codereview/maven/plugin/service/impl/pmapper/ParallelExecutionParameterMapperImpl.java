package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PParallelExecutionParameter;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.ParallelExecutionParameterMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.base.EnhancedMapperBase;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation.MapperValidationUtils;
import com.quasarbyte.llm.codereview.sdk.model.parameter.ParallelExecutionParameter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enhanced mapper implementation for PParallelExecutionParameter to ParallelExecutionParameter.
 * Extends EnhancedMapperBase for comprehensive error handling and validation.
 */
public class ParallelExecutionParameterMapperImpl extends EnhancedMapperBase implements ParallelExecutionParameterMapper {

    // Valid ranges for parallel execution parameters
    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 1000;
    private static final int MIN_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 100;
    
    // Default values
    private static final int DEFAULT_BATCH_SIZE = 1;
    private static final int DEFAULT_POOL_SIZE = 1;

    @Override
    protected String getMapperName() {
        return "ParallelExecutionParameterMapper";
    }

    @Override
    public ParallelExecutionParameter map(PParallelExecutionParameter parameter) {
        return executeMapping(() -> mapInternal(parameter), "map");
    }
    
    private ParallelExecutionParameter mapInternal(PParallelExecutionParameter parameter) {
        logger.debug("Starting parallel execution parameter mapping");
        
        if (parameter == null) {
            logger.debug("Input PParallelExecutionParameter is null, returning null");
            return null;
        }
        
        // Validate and process batch size
        Integer batchSize = validateAndProcessBatchSize(parameter.getBatchSize());
        
        // Validate and process pool size
        Integer poolSize = validateAndProcessPoolSize(parameter.getPoolSize());
        
        // Create executor service with validation
        ExecutorService executorService = createValidatedExecutorService(poolSize);
        
        // Create result
        ParallelExecutionParameter result = new ParallelExecutionParameter()
                .setBatchSize(batchSize)
                .setExecutorService(executorService);
        
        logMappingSuccess("PParallelExecutionParameter", "ParallelExecutionParameter", 
                         String.format("batchSize: %d, poolSize: %d", batchSize, poolSize));
        
        return result;
    }
    
    /**
     * Validates and processes batch size with proper defaults and ranges.
     * @param inputBatchSize the input batch size
     * @return validated batch size
     */
    private Integer validateAndProcessBatchSize(Integer inputBatchSize) {
        if (inputBatchSize == null) {
            logger.info("BatchSize is null, using default value: {}", DEFAULT_BATCH_SIZE);
            return DEFAULT_BATCH_SIZE;
        }
        
        if (inputBatchSize <= 0) {
            logger.warn("BatchSize is non-positive ({}), using default value: {}", inputBatchSize, DEFAULT_BATCH_SIZE);
            return DEFAULT_BATCH_SIZE;
        }
        
        // Validate range
        if (inputBatchSize < MIN_BATCH_SIZE || inputBatchSize > MAX_BATCH_SIZE) {
            throw new ValidationException(String.format(
                "BatchSize must be between %d and %d, but was: %d", 
                MIN_BATCH_SIZE, MAX_BATCH_SIZE, inputBatchSize));
        }
        
        logger.debug("Using provided batchSize: {}", inputBatchSize);
        return inputBatchSize;
    }
    
    /**
     * Validates and processes pool size with proper defaults and ranges.
     * @param inputPoolSize the input pool size
     * @return validated pool size
     */
    private Integer validateAndProcessPoolSize(Integer inputPoolSize) {
        if (inputPoolSize == null) {
            logger.info("PoolSize is null, using default value: {}", DEFAULT_POOL_SIZE);
            return DEFAULT_POOL_SIZE;
        }
        
        if (inputPoolSize <= 0) {
            logger.warn("PoolSize is non-positive ({}), using default value: {}", inputPoolSize, DEFAULT_POOL_SIZE);
            return DEFAULT_POOL_SIZE;
        }
        
        // Validate range
        if (inputPoolSize < MIN_POOL_SIZE || inputPoolSize > MAX_POOL_SIZE) {
            throw new ValidationException(String.format(
                "PoolSize must be between %d and %d, but was: %d", 
                MIN_POOL_SIZE, MAX_POOL_SIZE, inputPoolSize));
        }
        
        // Warn about potentially excessive pool sizes
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (inputPoolSize > availableProcessors * 2) {
            logger.warn("PoolSize ({}) is larger than 2x available processors ({}), this may not be optimal", 
                       inputPoolSize, availableProcessors);
        }
        
        logger.debug("Using provided poolSize: {}", inputPoolSize);
        return inputPoolSize;
    }
    
    /**
     * Creates a validated executor service.
     * @param poolSize the pool size
     * @return configured executor service
     */
    private ExecutorService createValidatedExecutorService(Integer poolSize) {
        try {
            logger.info("Creating ExecutorService with poolSize: {}", poolSize);
            ExecutorService executorService = Executors.newWorkStealingPool(poolSize);
            
            // Validate that executor service was created successfully
            if (executorService == null) {
                throw new ValidationException("Failed to create ExecutorService");
            }
            
            if (executorService.isShutdown()) {
                throw new ValidationException("Created ExecutorService is already shutdown");
            }
            
            logger.debug("ExecutorService created successfully: {}", executorService.getClass().getSimpleName());
            return executorService;
            
        } catch (Exception e) {
            logger.error("Failed to create ExecutorService with poolSize {}: {}", poolSize, e.getMessage(), e);
            throw new ValidationException("Failed to create ExecutorService: " + e.getMessage(), e);
        }
    }
    
    /**
     * Formats parameter for logging (safe for null values).
     * @param parameter the parameter to format
     * @return formatted string
     */
    private static String formatParameter(PParallelExecutionParameter parameter) {
        if (parameter == null) {
            return "null";
        }
        
        return String.format("PParallelExecutionParameter{batchSize=%s, poolSize=%s}", 
                           parameter.getBatchSize(), parameter.getPoolSize());
    }
}
