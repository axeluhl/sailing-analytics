package com.sap.sailing.server.masterdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.sap.sailing.domain.common.DataImportProgress;

public class DataImportLockWithProgress extends ReentrantLock {
    /**
     * Defaults to 60s (60000ms).
     */
    private static final int TIME_TO_DELETE_PROGRESS_ENTRY_AFTER_OPERATION_FINISHED_IN_MILLIS = 60000;

    private static final long serialVersionUID = -3527221613483691340L;

    private DataImportProgress currentProgress;

    private final Map<UUID, DataImportProgress> progressPerId;

    private final ReentrantReadWriteLock mapLock;

    public DataImportLockWithProgress() {
        super(true);
        progressPerId = new HashMap<UUID, DataImportProgress>();
        mapLock = new ReentrantReadWriteLock();
    }

    public void lock(UUID operationId) {
        super.lock();
        currentProgress = progressPerId.get(operationId);
    }

    @Override
    public void unlock() {
        setDeleteFromMapTimer(currentProgress);
        currentProgress = null;
        super.unlock();
    }

    /**
     * This timer ensures that operation results are deleted from memory after some time. Just deleting the entry when
     * the operation is done does not ensure that the entry is deleted from every replica, since the last progress
     * request will only reach one server.
     */
    private void setDeleteFromMapTimer(final DataImportProgress progressToDelete) {
        TimerTask deleteTask = new TimerTask() {
            @Override
            public void run() {
                mapLock.writeLock().lock();
                try {
                    progressPerId.remove(progressToDelete.getOperationId());
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
        mapLock.readLock().lock();
        try {
            progress = progressPerId.get(operationId);
        } finally {
            mapLock.readLock().unlock();
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
