package com.sap.sailing.ingestion.dto;

import java.io.Serializable;

public class AWSResponseHttpHeader implements Serializable {

    private static final long serialVersionUID = 5896178885597045708L;

    private String contentType;

    public static AWSResponseHttpHeader jsonResponse() {
        final AWSResponseHttpHeader result = new AWSResponseHttpHeader();
        result.setContentType("application/json");
        return result;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
