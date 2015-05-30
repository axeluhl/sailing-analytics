package com.sap.sailing.gwt.ui.shared.dispatch;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RequestWrapper<R extends Result, A extends Action<R>> implements IsSerializable {
    
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
