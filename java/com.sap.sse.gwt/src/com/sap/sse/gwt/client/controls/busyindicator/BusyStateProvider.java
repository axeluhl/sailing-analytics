package com.sap.sse.gwt.client.controls.busyindicator;


public interface BusyStateProvider {

    void addBusyStateChangeListener(BusyStateChangeListener listener);

    void removeBusyStateChangeListener(BusyStateChangeListener listener);
    
    void setBusyState(boolean isBusy);

    boolean isBusy();
}
