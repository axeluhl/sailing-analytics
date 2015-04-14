package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.settings.Settings;

public class TimePanelSettings implements Settings {
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
