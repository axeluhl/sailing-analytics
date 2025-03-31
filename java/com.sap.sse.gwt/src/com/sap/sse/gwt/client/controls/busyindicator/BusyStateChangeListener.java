package com.sap.sse.gwt.client.controls.busyindicator;


/**
 * Allows UI components to observe the busy state of an operation.
 */
public interface BusyStateChangeListener {

    /**
     * @param busyState the new busy state
     */
    void onBusyStateChange(boolean busyState);

}
