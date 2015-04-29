package com.sap.sailing.gwt.ui.client;

import com.sap.sse.common.settings.AbstractSettings;

public class TimePanelSettings extends AbstractSettings {
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
