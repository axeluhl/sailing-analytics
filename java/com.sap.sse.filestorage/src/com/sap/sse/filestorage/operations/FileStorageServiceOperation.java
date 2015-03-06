package com.sap.sse.filestorage.operations;

import com.sap.sse.filestorage.impl.ReplicableFileStorageManagementService;
import com.sap.sse.replication.OperationWithResult;

public interface FileStorageServiceOperation<ResultT> extends
        OperationWithResult<ReplicableFileStorageManagementService, ResultT> {

}
