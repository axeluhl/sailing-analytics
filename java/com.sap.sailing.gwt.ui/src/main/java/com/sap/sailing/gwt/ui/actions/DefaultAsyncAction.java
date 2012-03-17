package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DefaultAsyncAction<Result> implements AsyncAction<Result> {
    protected AsyncCallback<?> wrapperCallback;

    public AsyncCallback<?> getWrapperCallback() {
        return wrapperCallback;
    }

    public void setWrapperCallback(AsyncCallback<?> wrapperCallback) {
        this.wrapperCallback = wrapperCallback;
    }
}
