package com.sap.sailing.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AWSResponseHeader {

    private String contentType;

    public static AWSResponseHeader jsonResponse() {
        final AWSResponseHeader result = new AWSResponseHeader();
        result.setContentType("application/json");
        return result;
    }

    @JsonProperty("Content-Type")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

}
