package com.sap.sse.security.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SuccessInfo implements IsSerializable {

    public static final long serialVersionUID = -3044914225885460520L;
    
    private boolean successful;
    private String message;
    
    public SuccessInfo() {
    }
    
    public SuccessInfo(boolean successful, String message) {
        super();
        this.successful = successful;
        this.message = message;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }
    
    
}
