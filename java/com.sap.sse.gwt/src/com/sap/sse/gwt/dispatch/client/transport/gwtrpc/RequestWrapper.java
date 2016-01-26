package com.sap.sse.gwt.dispatch.client.transport.gwtrpc;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.gwt.dispatch.client.commands.Action;
import com.sap.sse.gwt.dispatch.client.commands.Result;
import com.sap.sse.gwt.dispatch.client.system.DispatchContext;

/**
 * Wrapper that encapsulates an action to be sent to the server.
 *
 * @param <A>
 *            Action
 * @param <R>
 *            Result
 * @param <CTX>
 *            Execution context
 */
public class RequestWrapper<R extends Result, A extends Action<R, CTX>, CTX extends DispatchContext> implements
        IsSerializable {
    
    private Date currentClientTime = new Date();
    
    private String clientLocaleName;
    
    private A action;
    
    @SuppressWarnings("unused")
    private RequestWrapper() {
    }
    
    public RequestWrapper(A action, String clientLocaleName) {
        super();
        this.action = action;
        this.clientLocaleName = clientLocaleName;
    }

    public A getAction() {
        return action;
    }
    
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
    
    public String getClientLocaleName() {
        return clientLocaleName;
    }
}
