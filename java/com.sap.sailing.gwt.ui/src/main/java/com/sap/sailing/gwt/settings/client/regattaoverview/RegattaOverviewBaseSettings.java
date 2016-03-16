package com.sap.sailing.gwt.settings.client.regattaoverview;

import java.util.UUID;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.BooleanSetting;
import com.sap.sse.common.settings.UUIDSetting;

public final class RegattaOverviewBaseSettings extends AbstractSettings {
    private final UUIDSetting event = new UUIDSetting("event", this);
    private final BooleanSetting ignoreLocalSettings = new BooleanSetting("ignoreLocalSettings", this, false);

    public RegattaOverviewBaseSettings(UUID event) {
        this(event, true);
    }
    
    public RegattaOverviewBaseSettings(UUID event, boolean ignoreLocalSettings) {
        this.event.setValue(event);
        this.ignoreLocalSettings.setValue(ignoreLocalSettings);
    }
}
