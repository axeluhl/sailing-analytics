package com.sap.sailing.domain.base.configuration;


/**
 * A {@link ConfigurationType} which is backed by some database. Provides methods to reload and save.
 */
public interface StoreableConfiguration<ConfigurationType extends StoreableConfiguration<?>> {
    
    /**
     * Reloads the configuration.
     * @return the same {@link ConfigurationType} after loading.
     */
    ConfigurationType load();
    
    
    /**
     * Saves the configuration
     * @return the same {@link ConfigurationType} after saving.
     */
    ConfigurationType store();
}
