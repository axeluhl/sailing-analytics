package com.sap.sailing.gwt.dispatch.client.rpcimpl;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.gwt.dispatch.client.Result;
import com.sap.sailing.gwt.dispatch.client.ResultWrapper;

public interface DispatchRPCAsync<CTX extends DispatchContext> {

    <R extends Result, A extends Action<R, CTX>> void execute(
            RequestWrapper<R, A, CTX> action,
            AsyncCallback<ResultWrapper<R>> callback);

}
