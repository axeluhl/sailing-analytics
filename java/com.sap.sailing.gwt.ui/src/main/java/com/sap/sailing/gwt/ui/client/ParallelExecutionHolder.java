package com.sap.sailing.gwt.ui.client;

public abstract class ParallelExecutionHolder {

    /** The number of successfully completed services */
    private int doneCount = 0;

    private ParallelExecutionCallback<?> asyncCallbacks[];

    protected ParallelExecutionHolder(ParallelExecutionCallback<?>... callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            throw new RuntimeException("No callbacks passed to execution holder");
        }
        this.asyncCallbacks = callbacks;
        for (ParallelExecutionCallback<?> callback : callbacks) {
            callback.setExecutionHolder(this);
        }
    }

    /**
     * Called by each async service call on completion. Only when all calls have been completed
     * the handleSuccess() is called.
     */
    protected synchronized void done() {
        doneCount++;
        if (doneCount == asyncCallbacks.length) {
            handleSuccess();
        }
    }

    /**
     * Called only when all parallel callbacks have been completed.
     */
    protected abstract void handleSuccess();

    /**
     * Called when one of the parallel callbacks has returned with a failure.
     */
    protected abstract void handleFailure(Throwable t);
}