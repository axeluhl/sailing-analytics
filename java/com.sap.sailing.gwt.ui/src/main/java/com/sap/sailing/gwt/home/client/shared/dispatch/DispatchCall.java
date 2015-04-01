package com.sap.sailing.gwt.home.client.shared.dispatch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

/**
 * The convenience class wrapping an synchronous call.
 *
 */
public final class DispatchCall<A extends Action<R>, R extends Result> {

    private final A action;

    private final AsyncCallback<R> callback;

    public DispatchCall(A action, AsyncCallback<R> callback) {
        this.action = action;
        this.callback = callback;
    }

    public Action<?> getAction() {
        return action;
    }

    public AsyncCallback<R> getCallback() {
        return callback;
    }

}