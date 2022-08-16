package com.sap.sse.filestorage.operations;

import com.sap.sse.filestorage.FileStorageService;
import com.sap.sse.filestorage.impl.ReplicableFileStorageManagementService;

public class SetActiveFileStorageServiceOperation implements FileStorageServiceOperation<Void> {
    private static final long serialVersionUID = 5385115508306732053L;
    protected final FileStorageService service;

    public SetActiveFileStorageServiceOperation(FileStorageService service) {
        this.service = service;
    }

    @Override
    public Void internalApplyTo(ReplicableFileStorageManagementService toState) throws Exception {
        toState.internalSetActiveFileStorageService(service);
        return null;
    }
}
