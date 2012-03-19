package com.sap.sailing.gwt.ui.actions;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AsyncAction<Result> {
    void execute();
    
    Result getResult();

    AsyncCallback<Result> getCallback();

    void setCallback(AsyncCallback<Result> callback);

    AsyncCallback<?> getWrapperCallback();

    void setWrapperCallback(AsyncCallback<Result> callback);

    String getName(); 
}
