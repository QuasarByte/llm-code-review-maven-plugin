package com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class providing common validation methods for mappers.
 * Centralized validation logic to ensure consistency across all mappers.
 */
public final class MapperValidationUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(MapperValidationUtils.class);
    
    // Common URL patterns
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("^jdbc:[a-zA-Z0-9]+://.*", Pattern.CASE_INSENSITIVE);
    
    private MapperValidationUtils() {
        // Utility class - prevent instantiation
    }
    
    /**
     * Validates that a required string is not null or blank.
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (nullOrBlank(value)) {
            String message = String.format("%s is required but not provided", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that an object is not null.
     * @param value the object to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            String message = String.format("%s cannot be null", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a collection is not null or empty.
     * @param collection the collection to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireNonEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            String message = String.format("%s cannot be null or empty", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a map is not null or empty.
     * @param map the map to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireNonEmpty(Map<?, ?> map, String fieldName) {
        if (map == null || map.isEmpty()) {
            String message = String.format("%s cannot be null or empty", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a string matches a specific pattern.
     * @param value the string to validate
     * @param pattern the pattern to match
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requirePattern(String value, Pattern pattern, String fieldName) {
        requireNonBlank(value, fieldName);
        if (!pattern.matcher(value.trim()).matches()) {
            String message = String.format("%s does not match required pattern: %s", fieldName, value);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a string is a valid HTTP/HTTPS URL.
     * @param url the URL to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireValidHttpUrl(String url, String fieldName) {
        requirePattern(url, HTTP_URL_PATTERN, fieldName);
    }
    
    /**
     * Validates that a string is a valid JDBC URL.
     * @param url the JDBC URL to validate
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireValidJdbcUrl(String url, String fieldName) {
        requireNonBlank(url, fieldName);
        if (!url.trim().toLowerCase().startsWith("jdbc:")) {
            String message = String.format("%s must start with 'jdbc:' - provided: %s", fieldName, maskSensitiveInfo(url));
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a numeric value is within a specified range.
     * @param value the value to validate
     * @param min the minimum allowed value (inclusive)
     * @param max the maximum allowed value (inclusive)
     * @param fieldName the name of the field for error messages
     * @throws ValidationException if validation fails
     */
    public static void requireInRange(Integer value, int min, int max, String fieldName) {
        if (value == null) {
            String message = String.format("%s cannot be null", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
        if (value < min || value > max) {
            String message = String.format("%s must be between %d and %d, but was: %d", fieldName, min, max, value);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates that a condition is true.
     * @param condition the condition to validate
     * @param message the error message if condition is false
     * @throws ValidationException if condition is false
     */
    public static void require(boolean condition, String message) {
        if (!condition) {
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Validates an object using a custom predicate.
     * @param value the value to validate
     * @param predicate the predicate to test
     * @param fieldName the name of the field for error messages
     * @param <T> the type of the value
     * @throws ValidationException if validation fails
     */
    public static <T> void requireValid(T value, Predicate<T> predicate, String fieldName) {
        requireNonNull(value, fieldName);
        if (!predicate.test(value)) {
            String message = String.format("%s failed validation", fieldName);
            logger.error("Validation failed: {}", message);
            throw new ValidationException(message);
        }
    }
    
    /**
     * Safely trims a string, returning null if input is null.
     * @param input the input string
     * @return trimmed string or null
     */
    public static String safeTrim(String input) {
        return input != null ? input.trim() : null;
    }
    
    /**
     * Checks if a string is null or blank.
     * @param string the string to check
     * @return true if the string is null or blank
     */
    public static boolean nullOrBlank(String string) {
        return string == null || string.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not null and not blank.
     * @param string the string to check
     * @return true if the string is not null and not blank
     */
    public static boolean notNullOrBlank(String string) {
        return !nullOrBlank(string);
    }
    
    /**
     * Masks sensitive information in strings for safe logging.
     * This method identifies common sensitive patterns and masks them.
     * @param input the input string
     * @return masked string for logging
     */
    public static String maskSensitiveInfo(String input) {
        if (input == null) {
            return null;
        }
        
        String masked = input;
        
        // Mask passwords in JDBC URLs
        masked = masked.replaceAll("password=[^;&]*", "password=***");
        masked = masked.replaceAll("pwd=[^;&]*", "pwd=***");
        
        // Mask API keys in URLs
        masked = masked.replaceAll("api[_-]?key=[^&]*", "api_key=***");
        masked = masked.replaceAll("token=[^&]*", "token=***");
        
        // Mask authorization headers
        masked = masked.replaceAll("(?i)authorization[\"']?\\s*[:=]\\s*[\"']?[^\"',\\s]*", "authorization=***");
        
        return masked;
    }
    
    /**
     * Logs validation success for debugging purposes.
     * @param fieldName the name of the field that was validated
     * @param value the value that was validated (will be masked for sensitive data)
     */
    public static void logValidationSuccess(String fieldName, Object value) {
        if (logger.isDebugEnabled()) {
            String safeValue = value instanceof String ? maskSensitiveInfo((String) value) : String.valueOf(value);
            logger.debug("Validation successful for {}: {}", fieldName, safeValue);
        }
    }
    
    /**
     * Logs mapping completion for debugging purposes.
     * @param sourceType the source type name
     * @param targetType the target type name
     * @param details additional details about the mapping
     */
    public static void logMappingSuccess(String sourceType, String targetType, String details) {
        logger.info("Successfully mapped {} to {} - {}", sourceType, targetType, details);
    }
}
