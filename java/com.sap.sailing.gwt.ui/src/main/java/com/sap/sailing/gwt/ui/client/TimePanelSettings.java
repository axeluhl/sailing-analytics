package com.sap.sailing.gwt.ui.client;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class TimePanelSettings extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    
    private static final long serialVersionUID = -4397130065617955193L;
    
    private LongSetting refreshInterval;
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        refreshInterval = new LongSetting("", this, 1000l);
    }

    public TimePanelSettings(long refreshInterval) {
        this();
        this.refreshInterval.setValue(refreshInterval);
    }

    public TimePanelSettings() {
        super(null);
    }

    public long getRefreshInterval() {
        return refreshInterval.getValue();
    }
}
