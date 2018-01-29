package com.sap.sailing.domain.tracking.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkQueue {
    private static final Logger LOG = Logger.getLogger(WorkQueue.class.getName());
    
    private final LinkedBlockingQueue<Runnable> queue;
    private Thread workThread;
    
    public WorkQueue() {
        queue = new LinkedBlockingQueue<>();
    }
    
    public synchronized void addWork(Runnable workToAdd) {
        queue.add(workToAdd);
        if (workThread == null) {
            workThread = new Thread(this::doWork);
            workThread.start();
        }
    }
    
    private void doWork() {
        while (true) {
            final Runnable workToDo;
            synchronized (this) {
                workToDo = queue.poll();
                if (workToDo == null) {
                    workThread = null;
                    break;
                }
            }
            if (workToDo != null) {
                try {
                    workToDo.run();
                } catch (Exception e) {
                    LOG.log(Level.SEVERE, "Error while executing work in queue", e);
                }
            }
        }
    }
}
