package com.sap.sse.gwt.dispatch.client.caching;

/**
 * Interface used by actions and results to provide cache time to live.
 *
 */
public interface HasClientCacheTotalTimeToLive {

    /**
     * How long should the result be stored in the client side cache
     * 
     * @return
     */
    int cacheTotalTimeToLiveMillis();
}
