package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PDataSourceConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PDataSourceConfigurationMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.validation.MapperValidationUtils;
import com.quasarbyte.llm.codereview.sdk.model.datasource.DataSourceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced mapper implementation for PDataSourceConfiguration to DataSourceConfiguration.
 * Provides comprehensive error handling, validation, and logging.
 */
public class PDataSourceConfigurationMapperImpl implements PDataSourceConfigurationMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(PDataSourceConfigurationMapperImpl.class);

    @Override
    public DataSourceConfiguration map(PDataSourceConfiguration source) {
        logger.debug("Starting data source configuration mapping for: {}", source);
        
        if (source == null) {
            logger.debug("Source PDataSourceConfiguration is null, returning null");
            return null;
        }

        try {
            // Validate input before mapping
            validateDataSourceConfiguration(source);
            
            DataSourceConfiguration result = new DataSourceConfiguration();
            
            // Map JDBC URL with validation
            String jdbcUrl = source.getJdbcUrl();
            MapperValidationUtils.requireValidJdbcUrl(jdbcUrl, "jdbcUrl");
            result.setJdbcUrl(jdbcUrl.trim());
            logger.debug("Set JDBC URL: {}", MapperValidationUtils.maskSensitiveInfo(jdbcUrl));
            
            // Map username - allow null/empty for some databases
            String username = source.getUsername();
            result.setUsername(username != null ? username.trim() : null);
            logger.debug("Set username: {}", username != null ? "[PROVIDED]" : "[NOT PROVIDED]");
            
            // Map password - allow null/empty for some databases
            String password = source.getPassword();
            result.setPassword(password);
            logger.debug("Set password: {}", password != null ? "[PROVIDED]" : "[NOT PROVIDED]");
            
            // Map driver class name with validation
            String driverClassName = source.getDriverClassName();
            if (MapperValidationUtils.nullOrBlank(driverClassName)) {
                logger.warn("Driver class name not provided, may cause issues if not auto-detected");
            } else {
                result.setDriverClassName(driverClassName.trim());
                logger.debug("Set driver class name: {}", driverClassName);
            }
            
            // Map properties with null safety
            Map<String, String> properties = source.getProperties();
            if (properties != null) {
                Map<String, String> cleanedProperties = new HashMap<>();
                for (Map.Entry<String, String> entry : properties.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        cleanedProperties.put(entry.getKey().trim(), entry.getValue().trim());
                    }
                }
                result.setProperties(cleanedProperties);
                logger.debug("Set properties with {} entries", cleanedProperties.size());
            } else {
                result.setProperties(null);
                logger.debug("No properties provided");
            }
            
            logger.info("Successfully mapped PDataSourceConfiguration - URL: {}, Driver: {}, Username: {}", 
                       MapperValidationUtils.maskSensitiveInfo(result.getJdbcUrl()),
                       result.getDriverClassName() != null ? result.getDriverClassName() : "auto-detect",
                       result.getUsername() != null ? "[PROVIDED]" : "[NOT PROVIDED]");
            
            return result;
            
        } catch (ValidationException e) {
            logger.error("Validation error during data source configuration mapping: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during data source configuration mapping: {}", e.getMessage(), e);
            throw new ValidationException("Failed to map data source configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the data source configuration input.
     * @param config the configuration to validate
     * @throws ValidationException if validation fails
     */
    private void validateDataSourceConfiguration(PDataSourceConfiguration config) {
        logger.debug("Validating PDataSourceConfiguration");
        
        MapperValidationUtils.requireNonNull(config, "PDataSourceConfiguration");
        
        // Validate JDBC URL
        String jdbcUrl = config.getJdbcUrl();
        MapperValidationUtils.requireValidJdbcUrl(jdbcUrl, "jdbcUrl");
        
        // Basic JDBC URL format validation
        if (!jdbcUrl.trim().toLowerCase().startsWith("jdbc:")) {
            throw new ValidationException("JDBC URL must start with 'jdbc:' - provided: " + MapperValidationUtils.maskSensitiveInfo(jdbcUrl));
        }
        
        // Validate that username and password are consistent (both null or both provided for some databases)
        String username = config.getUsername();
        String password = config.getPassword();
        
        // For security, we don't log the actual password
        if (MapperValidationUtils.notNullOrBlank(username) && password == null) {
            logger.warn("Username provided but password is null - this may cause authentication issues");
        }
        
        logger.debug("PDataSourceConfiguration validation completed successfully");
    }
}
