package com.sap.sailing.domain.tracking.impl;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Queue that executes {@link Runnable} instances in order on a distinct {@link Thread}. The {@link AsynchronousRunnableExecutor} ensures
 * that a Thread is started when the first {@link Runnable} is added via {@link #addWork(Runnable)} and that the
 * {@link Thread} is terminated after executing the last available {@link Runnable}.
 */
public class AsynchronousRunnableExecutor implements RunnableExecutor {
    private static final Logger LOG = Logger.getLogger(AsynchronousRunnableExecutor.class.getName());
    
    private final LinkedBlockingQueue<Runnable> queue;
    private Thread workThread;
    
    public AsynchronousRunnableExecutor() {
        queue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Adds the given {@link Runnable} to the {@link Queue} of work to be executed.
     * Ensures that a Thread is started if there isn't one running yet.
     */
    @Override
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
