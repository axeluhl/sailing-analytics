package com.sap.sailing.gwt.ui.client;

public class TimePanelSettings {
    /* must be of type long to allow manipulation of 'live' events with a very large delay */
    private long delayToLivePlayInSeconds;
    private long refreshInterval;

    public TimePanelSettings() {
        delayToLivePlayInSeconds = 0;
        refreshInterval = 1000;
    }

    public long getDelayToLivePlayInSeconds() {
        return delayToLivePlayInSeconds;
    }

    public void setDelayToLivePlayInSeconds(long delayToLivePlayInSeconds) {
        this.delayToLivePlayInSeconds = delayToLivePlayInSeconds;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

}
