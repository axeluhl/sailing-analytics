package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DefaultAsyncAction<Result> implements AsyncAction<Result> {
    protected AsyncCallback<Result> wrapperCallback;

    public AsyncCallback<Result> getWrapperCallback() {
        return wrapperCallback;
    }

    public void setWrapperCallback(AsyncCallback<Result> wrapperCallback) {
        this.wrapperCallback = wrapperCallback;
    }
}
