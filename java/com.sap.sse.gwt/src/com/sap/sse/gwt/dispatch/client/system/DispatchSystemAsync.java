package com.sap.sse.gwt.dispatch.client.system;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.commands.Action;
import com.sap.sse.gwt.dispatch.client.commands.Result;

/**
 * Async interface that defines the dispatch system contract.
 *
 * @param <CTX>
 *            the backend context that provides access to the services
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
