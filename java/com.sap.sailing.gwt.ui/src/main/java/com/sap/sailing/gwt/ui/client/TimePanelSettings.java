package com.sap.sailing.gwt.ui.client;

public class TimePanelSettings {
    private int delayToLivePlayInSeconds;
    private long refreshInterval;

    public TimePanelSettings() {
        delayToLivePlayInSeconds = 0;
        refreshInterval = 1000;
    }

    public int getDelayToLivePlayInSeconds() {
        return delayToLivePlayInSeconds;
    }

    public void setDelayToLivePlayInSeconds(int delayToLivePlayInSeconds) {
        this.delayToLivePlayInSeconds = delayToLivePlayInSeconds;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

}
