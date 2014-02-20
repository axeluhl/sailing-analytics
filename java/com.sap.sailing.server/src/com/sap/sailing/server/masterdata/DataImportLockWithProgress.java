package com.sap.sailing.server.masterdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.sap.sailing.domain.common.DataImportProgress;

public class DataImportLockWithProgress extends ReentrantLock {
    /**
     * Defaults to 60s (60000ms).
     */
    private static final int TIME_TO_DELETE_PROGRESS_ENTRY_AFTER_OPERATION_FINISHED_IN_MILLIS = 60000;

    private static final long serialVersionUID = -3527221613483691340L;

    private DataImportProgress currentProgress;

    private final Map<UUID, DataImportProgress> progressPerId;

    public DataImportLockWithProgress() {
        super(true);
        progressPerId = new HashMap<UUID, DataImportProgress>();
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
                synchronized (progressPerId) {
                    progressPerId.remove(progressToDelete.getOperationId());
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
        synchronized (progressPerId) {
            progress = progressPerId.get(operationId);
        }
        return progress;
    }

    public void addProgress(UUID operationId, DataImportProgress progress) {
        synchronized (progressPerId) {
            progressPerId.put(operationId, progress);
        }
    }


}
