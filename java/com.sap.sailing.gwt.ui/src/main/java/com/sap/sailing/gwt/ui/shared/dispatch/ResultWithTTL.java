package com.sap.sailing.gwt.ui.shared.dispatch;

public class ResultWithTTL<T extends DTO> implements Result {

    /**
     * Time To Live in millisecs.
     */
    private long ttl;

    private T dto;
    
    public ResultWithTTL() {
    }

    public ResultWithTTL(long ttl, T dto) {
        super();
        this.ttl = ttl;
        this.dto = dto;
    }

    public T getDto() {
        return dto;
    }

    public long getTtl() {
        return ttl;
    }
}
