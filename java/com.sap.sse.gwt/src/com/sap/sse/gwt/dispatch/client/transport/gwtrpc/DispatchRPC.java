package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.gwt.dispatch.client.commands.Action;
import com.sap.sse.gwt.dispatch.client.commands.Result;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

/**
 * GWT RPC interfaces for dispatch communication
 *
 * @param <CTX>
 */
public interface DispatchRPC<CTX extends DispatchContext> extends RemoteService {
    
    <R extends Result, A extends Action<R, CTX>> ResultWrapper<R> execute(
            RequestWrapper<R, A, CTX> action) throws DispatchException;

}
