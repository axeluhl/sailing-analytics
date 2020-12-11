package com.sap.sailing.ingestion.dto;

import java.io.Serializable;

public class AWSResponseWrapper<T> implements Serializable {

    private static final long serialVersionUID = -3550784751905818148L;

    private int statusCode;
    private String statusDescription;
    private AWSResponseHttpHeader headers;

    /**
     * Returns a response that signals callers that the request has been successful
     */
    public static <C> AWSResponseWrapper<C> successResponseAsJson(C input) {
        final AWSResponseWrapper<C> result = new AWSResponseWrapper<C>();
        result.setStatusCode(200);
        result.setStatusDescription("200 OK");
        result.setHeaders(AWSResponseHttpHeader.jsonResponse());
        result.setBody(input);
        return result;
    }

    private T body;

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public AWSResponseHttpHeader getHeaders() {
        return headers;
    }

    public void setHeaders(AWSResponseHttpHeader headers) {
        this.headers = headers;
    }

}
