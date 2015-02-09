package com.sap.sse.filestorage;

import java.io.Serializable;
import java.util.Locale;

/**
 * Property of a file storage service.
 * @author Fredrik Teschke
 *
 */
public interface FileStorageServiceProperty extends Serializable {
    boolean isRequired();
    String getName();
    
    /**
     * @return {@code null} if not yet initialized
     */
    String getValue();
    String getDescription(Locale locale);
}
