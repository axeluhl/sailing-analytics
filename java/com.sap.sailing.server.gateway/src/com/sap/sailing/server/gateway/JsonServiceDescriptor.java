package com.sap.sailing.server.gateway;

/**
 * A descriptor for json services 
 * @author Frank
 *
 */
public interface JsonServiceDescriptor {
    String getName();
    
    String getContextPath();

    String[] getMandatoryParameterNames();
    String[] getOptionalParameterNames();

    String getDescription();
    
    String getVersion();
}
