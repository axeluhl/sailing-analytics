package com.sap.sailing.gwt.ui.shared.dispatch.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;

public interface DispatchRPCAsync {

    <R extends Result, A extends Action<R>> void execute(Action<R> action, AsyncCallback<R> callback);

}
