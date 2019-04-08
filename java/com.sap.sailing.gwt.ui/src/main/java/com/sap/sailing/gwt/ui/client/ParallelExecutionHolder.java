package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Triggers the {@link #handleSuccess()} method after all {@link ParallelExecutionCallback}s that were passed to the
 * constructor have received their {@link AsyncCallback#onSuccess(Object)} call. All {@link ParallelExecutionCallback#onFailure(Throwable)}
 * callback will lead to the {@link #handleFailure(Throwable)} method being called.<p>
 * 
 * After creating the {@link ParallelExecutionCallback} objects and passing them to this class's constructor,
 * use them as regular callback objects in the GWT RPC service method invocations.
 * 
 * @author Axel Uhl (d043530)
 *
 */
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