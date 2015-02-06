package com.sap.sse.filestorage;


public interface FileStorageServiceResolver {
    /**
     * @return {@code null} if this service is not known
     */
    FileStorageService getFileStorageService(String serviceName);
}
