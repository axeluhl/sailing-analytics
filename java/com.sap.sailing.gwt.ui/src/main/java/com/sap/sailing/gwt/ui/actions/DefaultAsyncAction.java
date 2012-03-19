package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DefaultAsyncAction<Result> implements AsyncAction<Result> {
    private AsyncCallback<Result> wrapperCallback;
    private final AsyncCallback<Result> callback;
    private Result result;

    protected DefaultAsyncAction(AsyncCallback<Result> callback) {
        this.callback = callback;
    }
    
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
    public String getType() {
        return getClass().getName();
    }
    
    @Override
    public Result getResult() {
        return result;
    }
}
