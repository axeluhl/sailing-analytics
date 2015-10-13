package com.sap.sailing.gwt.ui.shared.dispatch;

import com.sap.sailing.gwt.home.shared.dispatch.HasClientCacheTotalTimeToLive;
import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class ResultWithTTL<T extends DTO> implements Result, HasClientCacheTotalTimeToLive {
    
    /** Time to live*/
    private Duration ttl;
    private T dto;
    
    @SuppressWarnings("unused")
    private ResultWithTTL() {
    }

    public ResultWithTTL(long ttl, T dto) {
       this(new MillisecondsDurationImpl(ttl), dto);
    }
    
    public ResultWithTTL(Duration ttl, T dto) {
        this.ttl = ttl;
        this.dto = dto;
    }

    public T getDto() {
        return dto;
    }

    public Duration getTtl() {
        return ttl;
    }
    
    public long getTtlMillis() {
        return ttl.asMillis();
    }

    @Override
    public int cacheTotalTimeToLiveMillis() {
        return (int) getTtlMillis();
    }
}
