package com.sap.sse.filestorage;

/**
 * Property of a file storage service.
 * @author Fredrik Teschke
 *
 */
public interface Property {
    boolean isRequired();
    String getName();
    
    /**
     * @return {@code null} if not yet initialized
     */
    String getValue();
    String getDescription();
}
