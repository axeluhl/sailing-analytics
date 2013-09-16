package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class DefaultAsyncAction<Result> implements AsyncAction<Result> {
    private final AsyncCallback<Result> callback;
    private Result result;

    protected DefaultAsyncAction(AsyncCallback<Result> callback) {
        this.callback = callback;
    }
    
    public AsyncCallback<Result> getWrapperCallback(final AsyncActionsExecutor asyncActionsExecutor) {
        // Wrap with action callback to hook into the call chain
        AsyncCallback<Result> wrapper = new AsyncCallback<Result>() {
            @Override
            public void onFailure(Throwable caught) {
                String type = DefaultAsyncAction.this.getType();
                GWT.log("Execution failure for action of type: " + type);
                AsyncCallback<Result> callback = DefaultAsyncAction.this.getCallback();
                callback.onFailure(caught);
                asyncActionsExecutor.callCompleted(type);
            }

            @Override
            public void onSuccess(Result result) {
                String type = DefaultAsyncAction.this.getType();
                GWT.log("Execution success for action of type: " + type);
                AsyncCallback<Result> callback = DefaultAsyncAction.this.getCallback();
                callback.onSuccess(result);
                asyncActionsExecutor.callCompleted(type);
            }
        };
        return wrapper;
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
