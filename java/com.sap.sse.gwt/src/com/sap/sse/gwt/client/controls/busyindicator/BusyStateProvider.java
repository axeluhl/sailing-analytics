package com.sap.sse.gwt.client.controls.busyindicator;


public interface BusyStateProvider {

    void addBusyStateChangeListener(BusyStateChangeListener listener);

    void removeBusyStateChangeListener(BusyStateChangeListener listener);
    
    boolean isBusy();

    /**
     * A new task has begun that keeps us busy
     */
    void addBusyTask();

    /**
     * A task that kept us busy has finished
     */
    void removeBusyTask();
}
