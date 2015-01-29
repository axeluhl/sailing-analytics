package com.sap.sse.filestorage.common;

/**
 * Property of a file storage service.
 * @author Fredrik Teschke
 *
 */
public interface FileStorageServiceProperty {
    boolean isRequired();
    String getName();
    
    /**
     * @return {@code null} if not yet initialized
     */
    String getValue();
    String getDescription();
}
