package com.sap.sailing.domain.tracking.impl;

/**
 * Executes {@link Runnable} tasks submitted to it. Depending on the implementation chosen this may happen synchronously
 * at the time the work is submitted, or asynchonously by some executor, for example.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface RunnableExecutor {
    void addWork(Runnable workToAdd);
}
