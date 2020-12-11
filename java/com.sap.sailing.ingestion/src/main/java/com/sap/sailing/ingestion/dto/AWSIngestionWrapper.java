package com.sap.sailing.ingestion.dto;

import java.io.Serializable;

public class AWSIngestionWrapper<T> implements Serializable {

    private static final long serialVersionUID = 6640992447269375968L;

    private T body;

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }

}
