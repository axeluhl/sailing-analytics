package com.sap.sailing.gwt.ui.shared.dispatch.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.RequestWrapper;
import com.sap.sailing.gwt.ui.shared.dispatch.Result;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWrapper;

public interface DispatchRPCAsync {

    <R extends Result, A extends Action<R>> void execute(RequestWrapper<R, A> action, AsyncCallback<ResultWrapper<R>> callback);

}
