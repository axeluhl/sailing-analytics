package com.sap.sailing.server.masterdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sailing.domain.common.DataImportProgress;
import com.sap.sailing.server.operationaltransformation.CreateOrUpdateDataImportProgress;
import com.sap.sailing.server.operationaltransformation.DataImportFailed;
import com.sap.sailing.server.operationaltransformation.ImportMasterDataOperation;
import com.sap.sailing.server.operationaltransformation.SetDataImportDeleteProgressFromMapTimer;
import com.sap.sailing.util.impl.LockUtil;
import com.sap.sailing.util.impl.NamedReentrantReadWriteLock;

/**
 * This lock is used to allow only one master data import operation be performed at once. It also keeps information
 * about the progress of the operation. It's important to keep in mind that there the {@link ImportMasterDataOperation}
 * is run on every replica and that any http request from the client to obtain the progress can be directed to any of
 * the servers by the load balancer. Thus, the progress needs to be maintained on every server. The first part of the
 * import (the data transfer) only happens on the server that the initial request went to. The progress is created and
 * updated on the other servers using a {@link CreateOrUpdateDataImportProgress} operation. If an error occurs it will
 * be logged to the progress object using the {@link DataImportFailed} operation.
 * 
 * The {@link #progressPerId} entries are deleted
 * {@value #TIME_TO_DELETE_PROGRESS_ENTRY_AFTER_OPERATION_FINISHED_IN_MILLIS} ms, after the operation finished on the
 * server reached with the initial import request. The {@link SetDataImportDeleteProgressFromMapTimer} operation is
 * replicated so that the map entry is also deleted on the other servers.
 * 
 * 
 * 
 * @author Frederik Petersen (D054528)
 * 
 */
public class DataImportLockWithProgress extends ReentrantLock {
    /**
     * Defaults to 60s (60000ms).
     */
    private static final int TIME_TO_DELETE_PROGRESS_ENTRY_AFTER_OPERATION_FINISHED_IN_MILLIS = 60000;

    private static final long serialVersionUID = -3527221613483691340L;

    private final Map<UUID, DataImportProgress> progressPerId;

    private final NamedReentrantReadWriteLock mapLock;

    public DataImportLockWithProgress() {
        super(true);
        progressPerId = new HashMap<UUID, DataImportProgress>();
        mapLock = new NamedReentrantReadWriteLock("mapLock in "+getClass().getName(), /* fair */ false);
    }

    /**
     * This timer ensures that operation results are deleted from memory after some time. Just deleting the entry when
     * the operation is done does not ensure that the entry is deleted from every replica, since the last progress
     * request will only reach one server.
     */
    public void setDeleteFromMapTimer(final UUID progressIDToDelete) {
        TimerTask deleteTask = new TimerTask() {
            @Override
            public void run() {
                mapLock.writeLock().lock();
                try {
                    progressPerId.remove(progressIDToDelete);
                } finally {
                    mapLock.writeLock().unlock();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(deleteTask, TIME_TO_DELETE_PROGRESS_ENTRY_AFTER_OPERATION_FINISHED_IN_MILLIS);
    }

    /**
     * @param operationId
     * @return the progress identified by the ID. Otherwise null.
     */
    public DataImportProgress getProgress(UUID operationId) {
        DataImportProgress progress;
        LockUtil.lockForRead(mapLock);
        try {
            progress = progressPerId.get(operationId);
        } finally {
            LockUtil.unlockAfterRead(mapLock);
        }
        return progress;
    }

    public void addProgress(UUID operationId, DataImportProgress progress) {
        mapLock.writeLock().lock();
        try {
            progressPerId.put(operationId, progress);
        } finally {
            mapLock.writeLock().unlock();
        }
    }


}
