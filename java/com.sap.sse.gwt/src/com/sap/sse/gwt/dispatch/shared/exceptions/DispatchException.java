package com.sap.sse.gwt.dispatch.shared.exceptions;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.google.gwt.user.client.rpc.IsSerializable;

public class DispatchException extends RuntimeException implements IsSerializable {
    private static final long serialVersionUID = 8192187255698006941L;
    private String exceptionId;
    
    protected DispatchException() {
    }

    @GwtIncompatible
    public DispatchException(String message) {
        super(message);
        exceptionId = UUID.randomUUID().toString();
    }

    public String getExceptionId() {
        return exceptionId;
    }

}
