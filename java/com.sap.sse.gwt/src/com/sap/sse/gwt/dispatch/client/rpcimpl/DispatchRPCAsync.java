package com.sap.sse.gwt.dispatch.client.rpcimpl;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.Action;
import com.sap.sse.gwt.dispatch.client.DispatchContext;
import com.sap.sse.gwt.dispatch.client.Result;

/**
 * Async interface
 *
 * @param <CTX>
 */
public interface DispatchRPCAsync<CTX extends DispatchContext> {

    <R extends Result, A extends Action<R, CTX>> void execute(
            RequestWrapper<R, A, CTX> action,
            AsyncCallback<ResultWrapper<R>> callback);

}
