package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestWrapper<R extends Result, A extends Action<R>> implements IsSerializable {
    
    private Date currentClientTime = new Date();
    
    private A action;
    
    @SuppressWarnings("unused")
    private RequestWrapper() {
    }
    
    public RequestWrapper(A action) {
        super();
        this.action = action;
    }

    public A getAction() {
        return action;
    }
    
    public Date getCurrentClientTime() {
        return currentClientTime;
    }
}
