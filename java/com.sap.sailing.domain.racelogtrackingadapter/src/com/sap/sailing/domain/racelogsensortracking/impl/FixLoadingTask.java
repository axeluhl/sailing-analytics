package com.sap.sailing.domain.racelogsensortracking.impl;

import java.util.logging.Logger;

import com.sap.sailing.domain.racelog.tracking.GPSFixStore;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

public class FixLoadingTask {
    private static final Logger logger = Logger.getLogger(FixLoadingTask.class.getName());
    
    private final NamedReentrantReadWriteLock loadingFromFixStoreLock = new NamedReentrantReadWriteLock("TODO", false);
    private final DynamicTrackedRace trackedRace;
    private boolean loadingFromGPSFixStore;
    
    public FixLoadingTask(DynamicTrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }
    
    public void loadFixesForLog(Runnable loadingAction, String description) {
        Thread t = new Thread(description) {
            @Override
            public void run() {
                trackedRace.lockForSerializationRead();
                LockUtil.lockForWrite(loadingFromFixStoreLock);
                synchronized (FixLoadingTask.this) {
                    loadingFromGPSFixStore = true; // indicates that the serialization lock is now safely held
                    FixLoadingTask.this.notifyAll();
                }
                
                try {
                    loadingAction.run();
                } finally {
                    synchronized (FixLoadingTask.this) {
                        loadingFromGPSFixStore = false;
                        FixLoadingTask.this.notifyAll();
                    }
                    LockUtil.unlockAfterWrite(loadingFromFixStoreLock);
                    trackedRace.unlockAfterSerializationRead();
                    logger.info("Thread "+getName()+" done.");
                }
            }
        };
        t.start();
//        if (waitForGPSFixesToLoad) {
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                logger.log(Level.WARNING, "Got interrupted while waiting for loading of GPS fixes from log "+log+" to finish", e);
//            }
//        }
    }
    
    public synchronized void waitForLoadingFromGPSFixStoreToFinishRunning() throws InterruptedException {
        while (loadingFromGPSFixStore) {
            wait();
        }
    }
    
    /**
     * Tells if currently the race is loading GPS fixes from the {@link GPSFixStore}. Clients may {@link Object#wait()} on <code>this</code>
     * object and will be notified whenever a change of this flag's value occurs.
     */
    public boolean isLoadingFromGPSFixStore() {
        return loadingFromGPSFixStore;
    }

}
