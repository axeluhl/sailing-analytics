package com.sap.sailing.gwt.ui.client;

public class TimePanelSettings {
    private long refreshInterval;

    public TimePanelSettings() {
        refreshInterval = 1000;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

}
