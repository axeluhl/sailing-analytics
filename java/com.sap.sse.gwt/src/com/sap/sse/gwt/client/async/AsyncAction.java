package com.sap.sse.gwt.client.async;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An action that which will be executed a asynchronous remote call to the server
 * 
 * @param <Result>
 *            The type of the returned value of the call
 * @author c5163874
 */
public interface AsyncAction<Result> {
    public void execute(AsyncCallback<Result> callback);
}
