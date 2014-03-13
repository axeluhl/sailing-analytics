package com.sap.sailing.domain.base.configuration;

import java.io.Serializable;
import java.util.List;

/**
 * Interface holding information about tablet's (RCApp) configuration.
 */
public interface DeviceConfiguration extends Serializable {
    
    /**
     * Course area names this tablet is allowed to log on.
     */
    List<String> getAllowedCourseAreaNames();
    
    /**
     * She is getting the result mails.
     */
    String getResultsMailRecipient();
    
    /**
     * Course names allowed in the By-Name-Course-Designer
     */
    List<String> getByNameCourseDesignerCourseNames();
    
    /**
     * Default configuration for all races without a custom {@link RegattaConfiguration}
     */
    RegattaConfiguration getRegattaConfiguration();
    
    /**
     * Copy me.
     */
    DeviceConfiguration copy();
}
