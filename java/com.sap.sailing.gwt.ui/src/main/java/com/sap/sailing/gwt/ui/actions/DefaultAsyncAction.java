package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DefaultAsyncAction<Result> implements AsyncAction<Result> {
    protected AsyncCallback<Result> wrapperCallback;
    private AsyncCallback<Result> callback;
    private Result result;

    public AsyncCallback<Result> getWrapperCallback() {
        return wrapperCallback;
    }

    public void setWrapperCallback(AsyncCallback<Result> wrapperCallback) {
        this.wrapperCallback = wrapperCallback;
    }

    @Override
    public AsyncCallback<Result> getCallback() {
        return callback;
    }

    @Override
    public void setCallback(AsyncCallback<Result> callback) {
        this.callback = callback;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
    
    @Override
    public Result getResult() {
        return result;
    }
}
