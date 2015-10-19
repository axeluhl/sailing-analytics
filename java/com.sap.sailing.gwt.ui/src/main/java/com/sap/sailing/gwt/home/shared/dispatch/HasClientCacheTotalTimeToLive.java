package com.sap.sailing.gwt.home.shared.dispatch;

/**
 * Interface used by actions and results to override default cache time to live.
 * 
 * @author pgtaboada
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
