package com.sap.sse.gwt.dispatch.shared.exceptions;

import com.google.gwt.core.shared.GwtIncompatible;

public class ServerDispatchException extends DispatchException {
    
    private static final long serialVersionUID = 8192187255698006941L;
    
    protected ServerDispatchException() {
    }

    @GwtIncompatible
    public ServerDispatchException(Throwable cause) {
        super(cause.getMessage());
    }

}
