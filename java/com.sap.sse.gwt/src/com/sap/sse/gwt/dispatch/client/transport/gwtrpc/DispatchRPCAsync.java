package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.gwt.dispatch.client.commands.Action;
import com.sap.sse.gwt.dispatch.client.commands.Result;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

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
