package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.performance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class providing performance optimizations for mappers.
 * Includes caching, null-safe operations, and efficient collection handling.
 */
public final class MapperPerformanceUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(MapperPerformanceUtils.class);
    
    // Simple cache for validation results to avoid repeated validations
    private static final Map<String, Boolean> validationCache = new ConcurrentHashMap<>();
    
    // Cache size limit to prevent memory leaks
    private static final int MAX_CACHE_SIZE = 1000;
    
    private MapperPerformanceUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Safely maps a collection with null checks and performance optimizations.
     * @param collection the collection to map
     * @param mapper the mapping function
     * @param <T> source type
     * @param <R> result type
     * @return mapped list, never null
     */
    public static <T, R> List<R> safeMapList(Collection<T> collection, Function<T, R> mapper) {
        if (collection == null || collection.isEmpty()) {
            return Collections.emptyList();
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            List<R> result = collection.stream()
                .filter(item -> item != null) // Filter out null items
                .map(mapper)
                .filter(mapped -> mapped != null) // Filter out null results
                .collect(Collectors.toList());
                
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 100) { // Log slow operations
                logger.debug("Mapped {} items in {} ms", collection.size(), duration);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error during collection mapping: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Safely maps a single item with null checks.
     * @param item the item to map
     * @param mapper the mapping function
     * @param <T> source type
     * @param <R> result type
     * @return mapped result or null if input is null
     */
    public static <T, R> R safeMap(T item, Function<T, R> mapper) {
        if (item == null) {
            return null;
        }
        
        try {
            return mapper.apply(item);
        } catch (Exception e) {
            logger.error("Error during item mapping: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Executes a mapping operation with performance monitoring.
     * @param operation the operation to execute
     * @param operationName the name for logging
     * @param <T> result type
     * @return operation result
     */
    public static <T> T withPerformanceMonitoring(Supplier<T> operation, String operationName) {
        long startTime = System.nanoTime();
        
        try {
            T result = operation.get();
            
            long durationNanos = System.nanoTime() - startTime;
            long durationMillis = durationNanos / 1_000_000;
            
            if (durationMillis > 50) { // Log operations taking more than 50ms
                logger.debug("Operation '{}' completed in {} ms", operationName, durationMillis);
            }
            
            return result;
            
        } catch (Exception e) {
            long durationNanos = System.nanoTime() - startTime;
            long durationMillis = durationNanos / 1_000_000;
            logger.error("Operation '{}' failed after {} ms: {}", operationName, durationMillis, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Checks if a validation result is cached to avoid repeated validations.
     * @param key the cache key
     * @return true if validation is cached and passed
     */
    public static boolean isCachedValidation(String key) {
        Boolean cached = validationCache.get(key);
        return cached != null && cached;
    }
    
    /**
     * Caches a validation result for future use.
     * @param key the cache key
     * @param valid the validation result
     */
    public static void cacheValidation(String key, boolean valid) {
        // Prevent cache from growing too large
        if (validationCache.size() >= MAX_CACHE_SIZE) {
            logger.debug("Validation cache size limit reached, clearing cache");
            validationCache.clear();
        }
        
        validationCache.put(key, valid);
    }
    
    /**
     * Creates a cache key for validation operations.
     * @param className the class name
     * @param fieldName the field name
     * @param value the value to validate
     * @return cache key
     */
    public static String createCacheKey(String className, String fieldName, Object value) {
        return String.format("%s.%s:%s", className, fieldName, 
                            value != null ? value.toString().hashCode() : "null");
    }
    
    /**
     * Efficiently checks if a collection is null or empty.
     * @param collection the collection to check
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    
    /**
     * Efficiently checks if a map is null or empty.
     * @param map the map to check
     * @return true if null or empty
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
    
    /**
     * Efficiently checks if a string is null or blank.
     * @param string the string to check
     * @return true if null or blank
     */
    public static boolean isNullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }
    
    /**
     * Safely gets the size of a collection, returning 0 for null collections.
     * @param collection the collection
     * @return size or 0 if null
     */
    public static int safeSize(Collection<?> collection) {
        return collection != null ? collection.size() : 0;
    }
    
    /**
     * Safely gets the size of a map, returning 0 for null maps.
     * @param map the map
     * @return size or 0 if null
     */
    public static int safeSize(Map<?, ?> map) {
        return map != null ? map.size() : 0;
    }
    
    /**
     * Safely trims a string, handling null input.
     * @param input the input string
     * @return trimmed string or null if input is null
     */
    public static String safeTrim(String input) {
        return input != null ? input.trim() : null;
    }
    
    /**
     * Safely converts a string to lowercase, handling null input.
     * @param input the input string
     * @return lowercase string or null if input is null
     */
    public static String safeLowerCase(String input) {
        return input != null ? input.toLowerCase() : null;
    }
    
    /**
     * Creates an immutable copy of a list with null safety.
     * @param list the source list
     * @param <T> element type
     * @return immutable copy or empty list if input is null
     */
    public static <T> List<T> safeImmutableCopy(List<T> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }
    
    /**
     * Creates an immutable copy of a map with null safety.
     * @param map the source map
     * @param <K> key type
     * @param <V> value type
     * @return immutable copy or empty map if input is null
     */
    public static <K, V> Map<K, V> safeImmutableCopy(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Logs performance statistics for debugging.
     * @param operationName the operation name
     * @param itemCount the number of items processed
     * @param durationMillis the duration in milliseconds
     */
    public static void logPerformanceStats(String operationName, int itemCount, long durationMillis) {
        if (logger.isDebugEnabled()) {
            double itemsPerSecond = itemCount > 0 && durationMillis > 0 ? 
                (itemCount * 1000.0) / durationMillis : 0;
            logger.debug("Performance: {} processed {} items in {} ms ({:.2f} items/sec)", 
                        operationName, itemCount, durationMillis, itemsPerSecond);
        }
    }
    
    /**
     * Clears the validation cache (useful for testing or memory management).
     */
    public static void clearValidationCache() {
        validationCache.clear();
        logger.debug("Validation cache cleared");
    }
    
    /**
     * Gets the current validation cache size (useful for monitoring).
     * @return cache size
     */
    public static int getValidationCacheSize() {
        return validationCache.size();
    }
}
