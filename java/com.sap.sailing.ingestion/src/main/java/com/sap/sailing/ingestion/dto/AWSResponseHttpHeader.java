package com.sap.sailing.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AWSResponseHttpHeader {

    private String contentType;

    public static AWSResponseHttpHeader jsonResponse() {
        final AWSResponseHttpHeader result = new AWSResponseHttpHeader();
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
