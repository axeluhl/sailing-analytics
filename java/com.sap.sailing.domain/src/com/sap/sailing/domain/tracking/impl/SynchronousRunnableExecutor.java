package com.sap.sailing.domain.tracking.impl;

/**
 * When work is {@link #addWork(Runnable) added} to this executor, it is executed immediately
 * by the thread invoking the {@link #addWork(Runnable)} method.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class SynchronousRunnableExecutor implements RunnableExecutor {
    @Override
    public void addWork(Runnable workToAdd) {
        workToAdd.run();
    }
}
