package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AsyncAction<Result> {
    void execute(AsyncActionsExecutor asyncActionsExecutor);
    
    Result getResult();

    AsyncCallback<Result> getCallback();

    AsyncCallback<Result> getWrapperCallback(AsyncActionsExecutor asyncActionsExecutor);

    String getType(); 
}
