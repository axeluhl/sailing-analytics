package com.sap.sailing.domain.base.configuration;


/**
 * Capable of loading and storing a {@link ConfigurationType}.
 */
public interface ConfigurationLoader<ConfigurationType> {
    
    /**
     * Loads the configuration from its database.
     * @return A final copy of {@link ConfigurationType} after loading.
     */
    ConfigurationType load();
    
    
    /**
     * Saves the configuration to its database.
     */
    void store();
}
