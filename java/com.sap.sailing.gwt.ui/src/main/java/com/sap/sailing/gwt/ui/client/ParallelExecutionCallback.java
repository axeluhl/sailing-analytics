package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The class is used together with the #{ParallelExecutionHolder} when two or more asynchronous service calls should run in parallel
 * and only in case all of them has been executed successfully the next action makes sense.
 */
public class ParallelExecutionCallback<T> implements AsyncCallback<T> {

    /** The data returned from the service call. */
    private T data;

    /** A reference to the execution holder of the parallel callbacks */
    private ParallelExecutionHolder parallelExecutionHolder;

    /**
     * The success method, which is called when the service call completes.
     */
    @Override
    public void onSuccess(T t) {
        this.data = t;
        parallelExecutionHolder.done();
    }

    /**
     * Gets the data returned from the async service call.
     */
    public T getData() {
        return data;
    }

    /**
     * Called by the execution holder to inject a reference to itself into the child.
     */
    protected void setExecutionHolder(ParallelExecutionHolder parallelExecutionHolder) {
        this.parallelExecutionHolder = parallelExecutionHolder;
    }

    /**
     * Handle method for a failure.
     */
    @Override
    public void onFailure(Throwable t) {
        this.parallelExecutionHolder.handleFailure(t);
    }
}
