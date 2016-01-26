package com.sap.sse.gwt.dispatch.client.commands;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.gwt.dispatch.client.exceptions.DispatchException;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;
import com.sap.sse.gwt.dispatch.client.system.DispatchSystemAsync;

/**
 * Action interface that defines actions that can be sent to server using a {@link DispatchSystemAsync}.
 * 
 * The execute method will be executed on the server side and is therefore {@link GwtIncompatible}.
 * 
 * The state defined in the action is the state that will be sent over the wire. Any method (other than execute, which
 * is GwtIncompatible) can be called both on the client and on the server side.
 *
 * @param <R>
 * @param <CTX>
 */
public interface Action<R extends Result, CTX extends DispatchContext> extends IsSerializable {

    /**
     * Server side execution of the action. The execute method is GwtIncompatible and will be available only on the
     * server side.
     * 
     * @param ctx
     * @return
     * @throws DispatchException
     */
    @GwtIncompatible
    R execute(CTX ctx) throws DispatchException;

}