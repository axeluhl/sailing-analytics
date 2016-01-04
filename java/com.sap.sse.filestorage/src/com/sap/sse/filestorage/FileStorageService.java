package com.sap.sse.filestorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.Locale;

import com.sap.sse.common.IsManagedByCache;
import com.sap.sse.common.TypeBasedServiceFinder;

/**
 * Service for storing files. The interface is intentionally agnostic of the underlying implementation, which may be
 * provided e.g. by using Amazon's S3 or simply saving the files to a statically reachable directory on one of our
 * servers. Implementations of this service are announced via the OSGi service registry. They should provide their name
 * as the OSGi property {@link TypeBasedServiceFinder#TYPE type};
 * 
 * TODO The storage service to use should be configured via the AdminConsole. Upon auto-discovering the available
 * services, the AdminConsole should allow to edit properties for each service (e.g. access credentials for AWS S3). We
 * probably need a generic property discovery mechanism (key-value based?), and to survive server restarts these
 * properties should be saved to the MongoDB.
 * 
 * @author Fredrik Teschke
 *
 */
public interface FileStorageService extends IsManagedByCache<FileStorageServiceResolver>, Serializable {
    /**
     * @param originalFileExtension
     *            may be {@code null}
     */
    URI storeFile(InputStream is, String fileExtension, long lengthInBytes) throws IOException,
            OperationFailedException, InvalidPropertiesException;

    /**
     * From the given {@code uri} it should be possible to determine the file to remove.
     */
    void removeFile(URI uri) throws OperationFailedException, InvalidPropertiesException, IOException;

    FileStorageServiceProperty[] getProperties();

    /**
     * Should not be called directly, but through {@link FileStorageManagementService#setFileStorageServiceProperty}
     * as this deals with storing the new values.
     * @throws IllegalArgumentException
     *             if {@code name} is not a valid property name
     */
    void internalSetProperty(String name, String value) throws IllegalArgumentException;

    String getName();

    /**
     * Description text that explains the storage service.
     */
    String getDescription(Locale locale);

    /**
     * Test whether properties are valid, e.g. by trying to log in using access credentials provided as properties.
     */
    void testProperties() throws InvalidPropertiesException, IOException;
}
