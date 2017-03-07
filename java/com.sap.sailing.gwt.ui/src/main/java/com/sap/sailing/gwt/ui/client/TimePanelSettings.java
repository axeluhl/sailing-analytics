package com.sap.sailing.gwt.ui.client;

import com.sap.sse.common.settings.AbstractSettings;

public class TimePanelSettings extends AbstractSettings {
    private final long refreshInterval;

    public TimePanelSettings(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }
}
