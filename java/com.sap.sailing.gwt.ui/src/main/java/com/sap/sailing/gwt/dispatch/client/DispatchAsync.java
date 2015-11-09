package com.sap.sailing.gwt.dispatch.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DispatchAsync<CTX extends DispatchContext> {
    
    <R extends Result, A extends Action<R, CTX>> void execute(A action, AsyncCallback<R> callback);

}
