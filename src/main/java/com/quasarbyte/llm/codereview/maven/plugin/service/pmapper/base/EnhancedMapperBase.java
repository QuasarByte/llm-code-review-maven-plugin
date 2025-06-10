package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.base;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.performance.MapperPerformanceUtils;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation.MapperValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enhanced base class for all mappers providing common functionality.
 * Includes validation, performance monitoring, and error handling utilities.
 */
public abstract class EnhancedMapperBase {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Gets the mapper name for logging and error messages.
     * @return the mapper name
     */
    protected abstract String getMapperName();
    
    /**
     * Executes a mapping operation with comprehensive error handling and performance monitoring.
     * @param operation the mapping operation
     * @param operationName the operation name for logging
     * @param <T> result type
     * @return mapping result
     * @throws ValidationException if validation fails
     */
    protected <T> T executeMapping(Supplier<T> operation, String operationName) {
        logger.debug("Starting {} operation in {}", operationName, getMapperName());
        
        return MapperPerformanceUtils.withPerformanceMonitoring(() -> {
            try {
                T result = operation.get();
                logger.debug("Successfully completed {} operation in {}", operationName, getMapperName());
                return result;
                
            } catch (ValidationException e) {
                logger.error("Validation error in {} during {}: {}", getMapperName(), operationName, e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error in {} during {}: {}", getMapperName(), operationName, e.getMessage(), e);
                throw new ValidationException("Failed during " + operationName + " in " + getMapperName() + ": " + e.getMessage(), e);
            }
        }, getMapperName() + "." + operationName);
    }
    
    /**
     * Maps a list with comprehensive error handling and performance optimization.
     * @param list the list to map
     * @param mapper the mapping function
     * @param itemName the item name for logging
     * @param <T> source type
     * @param <R> result type
     * @return mapped list
     */
    protected <T, R> List<R> mapList(List<T> list, Function<T, R> mapper, String itemName) {
        if (list == null) {
            logger.debug("Input list is null for {}, returning empty list", itemName);
            return java.util.Collections.emptyList();
        }
        
        logger.debug("Mapping {} {} items", list.size(), itemName);
        
        return MapperPerformanceUtils.safeMapList(list, item -> {
            try {
                return mapper.apply(item);
            } catch (Exception e) {
                logger.error("Failed to map {} item: {}", itemName, e.getMessage(), e);
                throw new ValidationException("Failed to map " + itemName + ": " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Maps a single item with error handling.
     * @param item the item to map
     * @param mapper the mapping function
     * @param itemName the item name for logging
     * @param <T> source type
     * @param <R> result type
     * @return mapped item
     */
    protected <T, R> R mapItem(T item, Function<T, R> mapper, String itemName) {
        if (item == null) {
            logger.debug("Input {} is null, returning null", itemName);
            return null;
        }
        
        return MapperPerformanceUtils.safeMap(item, mappedItem -> {
            try {
                return mapper.apply(mappedItem);
            } catch (Exception e) {
                logger.error("Failed to map {}: {}", itemName, e.getMessage(), e);
                throw new ValidationException("Failed to map " + itemName + ": " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Validates a required field and logs the validation.
     * @param value the value to validate
     * @param fieldName the field name
     * @param <T> value type
     * @return the validated value
     */
    protected <T> T validateRequired(T value, String fieldName) {
        MapperValidationUtils.requireNonNull(value, fieldName);
        MapperValidationUtils.logValidationSuccess(fieldName, value);
        return value;
    }
    
    /**
     * Validates a required string field and logs the validation.
     * @param value the value to validate
     * @param fieldName the field name
     * @return the validated and trimmed value
     */
    protected String validateRequiredString(String value, String fieldName) {
        MapperValidationUtils.requireNonBlank(value, fieldName);
        String trimmed = MapperValidationUtils.safeTrim(value);
        MapperValidationUtils.logValidationSuccess(fieldName, trimmed);
        return trimmed;
    }
    
    /**
     * Validates an optional string field and logs the validation.
     * @param value the value to validate
     * @param fieldName the field name
     * @return the trimmed value or null if blank
     */
    protected String validateOptionalString(String value, String fieldName) {
        if (MapperValidationUtils.nullOrBlank(value)) {
            logger.debug("Optional field {} is null or blank", fieldName);
            return null;
        }
        
        String trimmed = MapperValidationUtils.safeTrim(value);
        MapperValidationUtils.logValidationSuccess(fieldName, trimmed);
        return trimmed;
    }
    
    /**
     * Validates a numeric range and logs the validation.
     * @param value the value to validate
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @param fieldName the field name
     * @return the validated value
     */
    protected Integer validateRange(Integer value, int min, int max, String fieldName) {
        if (value != null) {
            MapperValidationUtils.requireInRange(value, min, max, fieldName);
            MapperValidationUtils.logValidationSuccess(fieldName, value);
        }
        return value;
    }
    
    /**
     * Validates a URL format and logs the validation.
     * @param url the URL to validate
     * @param fieldName the field name
     * @return the validated URL
     */
    protected String validateUrl(String url, String fieldName) {
        if (MapperValidationUtils.nullOrBlank(url)) {
            return null;
        }
        
        MapperValidationUtils.requireValidHttpUrl(url, fieldName);
        String trimmed = MapperValidationUtils.safeTrim(url);
        MapperValidationUtils.logValidationSuccess(fieldName, MapperValidationUtils.maskSensitiveInfo(trimmed));
        return trimmed;
    }
    
    /**
     * Logs successful mapping completion.
     * @param sourceType the source type name
     * @param targetType the target type name
     * @param details additional details
     */
    protected void logMappingSuccess(String sourceType, String targetType, String details) {
        MapperValidationUtils.logMappingSuccess(sourceType, targetType, details);
    }
    
    /**
     * Creates a cached validation key for performance optimization.
     * @param fieldName the field name
     * @param value the value
     * @return cache key
     */
    protected String createValidationCacheKey(String fieldName, Object value) {
        return MapperPerformanceUtils.createCacheKey(getMapperName(), fieldName, value);
    }
    
    /**
     * Checks if validation is cached.
     * @param cacheKey the cache key
     * @return true if cached and valid
     */
    protected boolean isCachedValidation(String cacheKey) {
        return MapperPerformanceUtils.isCachedValidation(cacheKey);
    }
    
    /**
     * Caches validation result.
     * @param cacheKey the cache key
     * @param valid the validation result
     */
    protected void cacheValidation(String cacheKey, boolean valid) {
        MapperPerformanceUtils.cacheValidation(cacheKey, valid);
    }
}
