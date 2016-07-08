package com.sap.sailing.gwt.settings.client.regattaoverview;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.UUIDSetting;

public final class RegattaOverviewBaseSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = -8541790130000694098L;
    private transient UUIDSetting event;
    private transient BooleanSetting ignoreLocalSettings;

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
