package com.sap.sailing.gwt.home.client.shared.dispatch;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public interface DispatchAsync {
    
    <R extends Result, A extends Action<R>> void execute(A action, AsyncCallback<R> callback);

}
