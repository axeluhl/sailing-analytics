package com.sap.sailing.domain.tractracadapter.impl;

import com.sap.sailing.domain.tractracadapter.CourseUpdateResponse;

public class CourseUpdateResponseImpl implements CourseUpdateResponse {
    
    private final String status;
    private final String message;
    
    public CourseUpdateResponseImpl(String status, String message) {
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
