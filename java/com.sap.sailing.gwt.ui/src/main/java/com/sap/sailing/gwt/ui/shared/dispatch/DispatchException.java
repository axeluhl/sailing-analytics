package com.sap.sailing.gwt.ui.shared.dispatch;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DispatchException extends RuntimeException implements IsSerializable {
    
    private static final long serialVersionUID = 8192187255698006941L;
    
    protected DispatchException() {
    }

    public DispatchException(String message) {
        super(message);
    }

}
