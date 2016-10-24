package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.shared.commands.Action;
import com.sap.sse.gwt.dispatch.shared.commands.Result;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * GWT RPC interfaces for dispatch communication
 *
 * @param <CTX>
 */
public interface DispatchRPC<CTX extends DispatchContext> extends RemoteService {
    
    <R extends Result, A extends Action<R, CTX>> ResultWrapper<R> execute(
            RequestWrapper<R, A, CTX> action) throws DispatchException;

}
