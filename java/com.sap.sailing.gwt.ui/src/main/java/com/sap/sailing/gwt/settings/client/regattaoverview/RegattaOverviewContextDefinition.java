package com.sap.sailing.gwt.settings.client.regattaoverview;

import java.util.UUID;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.UUIDSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public final class RegattaOverviewContextDefinition extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    private static final long serialVersionUID = -8541790130000694098L;
    private transient UUIDSetting event;

    public RegattaOverviewContextDefinition() {
        super(null);
    }
    
    public RegattaOverviewContextDefinition(String event) {
        this(UUID.fromString(event));
    }
    
    public RegattaOverviewContextDefinition(UUID event) {
        this();
        this.event.setValue(event);
    }
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        event = new UUIDSetting("event", this);
    }
    
    public UUID getEvent() {
        return event.getValue();
    }
}
