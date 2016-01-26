package com.sap.sse.gwt.dispatch.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async interface that defines the dispatch system contract.
 *
 * @param <CTX>
 */
public interface DispatchSystemAsync<CTX extends DispatchContext> {
    
    /**
     * Send an action to be executed on the server.
     * 
     * @param action
     *            the action to be executed on the server
     * 
     * @param callback
     *            the callback that will called upon success or failure.
     */
    <R extends Result, A extends Action<R, CTX>> void execute(A action, AsyncCallback<R> callback);

}
