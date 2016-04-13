package com.sap.sailing.gwt.settings.client.regattaoverview;

import java.util.UUID;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.BooleanSetting;
import com.sap.sse.common.settings.UUIDSetting;

public final class RegattaOverviewBaseSettings extends AbstractSettings {
    private UUIDSetting event;
    private BooleanSetting ignoreLocalSettings;

    public RegattaOverviewBaseSettings() {
    }
    
    public RegattaOverviewBaseSettings(String event) {
        this(UUID.fromString(event));
    }
    
    public RegattaOverviewBaseSettings(UUID event) {
        this(event, true);
    }
    
    @Override
    protected void addChildSettings() {
        event = new UUIDSetting("event", this);
        ignoreLocalSettings = new BooleanSetting("ignoreLocalSettings", this, false);
    }
    
    public RegattaOverviewBaseSettings(UUID event, boolean ignoreLocalSettings) {
        this.event.setValue(event);
        this.ignoreLocalSettings.setValue(ignoreLocalSettings);
    }
    
    public UUID getEvent() {
        return event.getValue();
    }
    
    public boolean isIgnoreLocalSettings() {
        return ignoreLocalSettings.getValue();
    }
}
