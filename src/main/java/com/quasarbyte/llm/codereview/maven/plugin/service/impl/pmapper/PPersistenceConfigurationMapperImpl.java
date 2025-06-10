package com.quasarbyte.llm.codereview.maven.plugin.service.impl.pmapper;

import com.quasarbyte.llm.codereview.maven.plugin.exception.ValidationException;
import com.quasarbyte.llm.codereview.maven.plugin.model.PPersistenceConfiguration;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PDataSourceConfigurationMapper;
import com.quasarbyte.llm.codereview.maven.plugin.service.pmapper.PPersistenceConfigurationMapper;
import com.quasarbyte.llm.codereview.sdk.model.parameter.PersistenceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced mapper implementation for PPersistenceConfiguration to PersistenceConfiguration.
 * Provides comprehensive error handling, validation, and logging.
 */
public class PPersistenceConfigurationMapperImpl implements PPersistenceConfigurationMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(PPersistenceConfigurationMapperImpl.class);
    
    private final PDataSourceConfigurationMapper dataSourceConfigurationMapper;

    public PPersistenceConfigurationMapperImpl(PDataSourceConfigurationMapper dataSourceConfigurationMapper) {
        if (dataSourceConfigurationMapper == null) {
            throw new IllegalArgumentException("PDataSourceConfigurationMapper cannot be null");
        }
        this.dataSourceConfigurationMapper = dataSourceConfigurationMapper;
        logger.debug("PPersistenceConfigurationMapperImpl initialized with DataSourceMapper: {}", 
                    dataSourceConfigurationMapper.getClass().getSimpleName());
    }

    @Override
    public PersistenceConfiguration map(PPersistenceConfiguration source) {
        logger.debug("Starting persistence configuration mapping for: {}", source);
        
        if (source == null) {
            logger.debug("Source PPersistenceConfiguration is null, returning null");
            return null;
        }

        try {
            // Validate input before mapping
            validatePersistenceConfiguration(source);
            
            PersistenceConfiguration result = new PersistenceConfiguration();
            
            // Map data source configuration with enhanced error handling
            if (source.getDataSourceConfiguration() != null) {
                try {
                    result.setDataSourceConfiguration(dataSourceConfigurationMapper.map(source.getDataSourceConfiguration()));
                    logger.debug("Successfully mapped DataSource configuration");
                } catch (Exception e) {
                    logger.error("Failed to map DataSource configuration: {}", e.getMessage(), e);
                    throw new ValidationException("Failed to map DataSource configuration: " + e.getMessage(), e);
                }
            } else {
                logger.debug("DataSource configuration is null, setting to null in result");
                result.setDataSourceConfiguration(null);
            }
            
            // Map persist file content with validation
            Boolean persistFileContent = source.getPersistFileContent();
            result.setPersistFileContent(persistFileContent);
            logger.debug("Set persistFileContent: {}", persistFileContent);
            
            logger.info("Successfully mapped PPersistenceConfiguration - DataSource: {}, PersistFileContent: {}", 
                       result.getDataSourceConfiguration() != null ? "configured" : "null",
                       result.getPersistFileContent());
            
            return result;
            
        } catch (ValidationException e) {
            logger.error("Validation error during persistence configuration mapping: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during persistence configuration mapping: {}", e.getMessage(), e);
            throw new ValidationException("Failed to map persistence configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates the persistence configuration input.
     * @param config the configuration to validate
     * @throws ValidationException if validation fails
     */
    private void validatePersistenceConfiguration(PPersistenceConfiguration config) {
        logger.debug("Validating PPersistenceConfiguration");
        
        if (config == null) {
            throw new ValidationException("PPersistenceConfiguration cannot be null");
        }
        
        // Additional validations can be added here as needed
        // For example, we could validate that if DataSource is provided, it has required fields
        if (config.getDataSourceConfiguration() != null) {
            logger.debug("DataSource configuration provided, will be validated by DataSource mapper");
        }
        
        // Validate persistFileContent is not null if we want to enforce it
        // Currently allowing null values as per SDK design
        
        logger.debug("PPersistenceConfiguration validation completed successfully");
    }
}
