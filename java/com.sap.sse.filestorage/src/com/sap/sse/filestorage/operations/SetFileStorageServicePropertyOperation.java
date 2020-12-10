package com.sap.sse.filestorage.operations;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.impl.ReplicableFileStorageManagementService;

public class SetFileStorageServicePropertyOperation implements FileStorageServiceOperation<Void> {
    private static final long serialVersionUID = 8583825216475534014L;
    protected final FileStorageService service;
    protected final String propertyName;
    protected final String propertyValue;

    public SetFileStorageServicePropertyOperation(FileStorageService service, String propertyName,
            String propertyValue) {
        this.service = service;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public Void internalApplyTo(ReplicableFileStorageManagementService toState) throws Exception {
        toState.internalSetFileStorageServiceProperty(service, propertyName, propertyValue);
        return null;
    }
}
