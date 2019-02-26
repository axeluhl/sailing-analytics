package com.sap.sse.gwt.server;

public interface RPCSerializedResultCacheMXBean {
    long getRecalcCount();
    long getCallCount();
    long getNumberOfCachedResults();
    long getTotalCacheSize();
}
