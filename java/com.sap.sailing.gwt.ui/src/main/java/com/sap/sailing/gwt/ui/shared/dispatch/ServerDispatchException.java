package com.sap.sailing.gwt.ui.shared.dispatch;


public class ServerDispatchException extends DispatchException {
    
    private static final long serialVersionUID = 8192187255698006941L;
    private String uuid;
    
    protected ServerDispatchException() {
    }

    public ServerDispatchException(String uuid, Throwable cause) {
        super(cause.getMessage());
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

}
