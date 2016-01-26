package com.sap.sse.gwt.dispatch.client.system.batching;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.commands.Action;
import com.sap.sse.gwt.dispatch.client.commands.Result;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

/**
 * The convenience class wrapping an synchronous call.
 *
 */
public final class DispatchCall<A extends Action<R, CTX>, R extends Result, CTX extends DispatchContext> {

    private final A action;

    private final AsyncCallback<R> callback;

    public DispatchCall(A action, AsyncCallback<R> callback) {
        this.action = action;
        this.callback = callback;
    }

    public Action<?, CTX> getAction() {
        return action;
    }

    public AsyncCallback<R> getCallback() {
        return callback;
    }

}