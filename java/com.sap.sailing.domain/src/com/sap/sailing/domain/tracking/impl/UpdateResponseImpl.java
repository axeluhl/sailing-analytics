package com.sap.sailing.domain.tracking.impl;

public class UpdateResponseImpl implements UpdateResponse {
    
    private final String status;
    private final String message;
    
    public UpdateResponseImpl(String status, String message) {
        this.status = status;
        this.message = message;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
