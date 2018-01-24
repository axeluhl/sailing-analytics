package com.sap.sailing.domain.tracking.impl;

import java.util.concurrent.LinkedBlockingQueue;

public class WorkQueue {
    
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
                    // TODO: handle exception
                }
            }
        }
    }
}
