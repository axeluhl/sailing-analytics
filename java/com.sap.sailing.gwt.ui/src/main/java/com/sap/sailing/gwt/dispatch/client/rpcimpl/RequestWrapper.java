package com.sap.sailing.gwt.dispatch.client.rpcimpl;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.gwt.dispatch.client.Action;
import com.sap.sailing.gwt.dispatch.client.DispatchContext;
import com.sap.sailing.gwt.dispatch.client.Result;

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
