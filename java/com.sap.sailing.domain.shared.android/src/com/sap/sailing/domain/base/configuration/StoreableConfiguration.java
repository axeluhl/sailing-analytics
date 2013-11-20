package com.sap.sailing.domain.base.configuration;


/**
 * A {@link ConfigurationType} which is backed by some database. Provides methods to reload and save.
 */
public interface StoreableConfiguration<ConfigurationType> {
    
    /**
     * Reloads the configuration.
     * @return A copy of this {@link ConfigurationType} after loading.
     */
    ConfigurationType load();
    
    
    /**
     * Saves the configuration
     */
    void store();
}
