package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

/**
 * The class is used together with the {@link ParallelExecutionHolder} when two or more asynchronous service calls
 * should run in parallel and only in case all of them have been executed successfully the next action makes sense.
 * Instances of this class are first created, then passed to a
 * {@link ParallelExecutionHolder#ParallelExecutionHolder(ParallelExecutionCallback...)} constructor and can then be
 * passed as callbacks to GWT RPC method invocations like the regular {@link AsyncCallback} and may also be wrapped by
 * {@link MarkedAsyncCallback} objects. The {@link #onSuccess(Object)} method tells the {@link ParallelExecutionHolder}
 * to which this callback is submitted that the call succeeded. If that was the last outstanding callback then the
 * {@link ParallelExecutionHolder} will subsequently invoke its {@link ParallelExecutionHolder#handleSuccess()} method.
 * <p>
 * 
 * Should this callback receive an {@link #onFailure(Throwable)} call then the
 * {@link ParallelExecutionHolder#handleFailure(Throwable)} method will be called. When this happens for at least one of
 * the callbacks registered with the {@link ParallelExecutionHolder} then the
 * {@link ParallelExecutionHolder#handleSuccess()} method will not be called.
 * <p>
 * 
 * In case of subclassing and overriding this class's default implementations of {@link #onSuccess(Object)} the subclass
 * implementation must invoke {@code super.onSuccess(t)} at some point during method execution.
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
