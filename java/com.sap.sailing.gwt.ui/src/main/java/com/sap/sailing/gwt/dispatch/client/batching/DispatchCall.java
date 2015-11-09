package com.sap.sailing.gwt.dispatch.client.batching;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.gwt.dispatch.client.Result;

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