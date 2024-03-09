package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

public class IgnoreLocalSettings extends AbstractGenericSerializableSettings<Object> {
    private static final long serialVersionUID = -282780795782313106L;
    
    private transient BooleanSetting ignoreLocalSettings;
    
    public IgnoreLocalSettings() {
        super(null);
    }
    
    public IgnoreLocalSettings(boolean ignoreLocalSettings) {
        this();
        this.ignoreLocalSettings.setValue(ignoreLocalSettings);
    }
    
    @Override
    protected void addChildSettings(Object context) {
        ignoreLocalSettings = new BooleanSetting("ignoreLocalSettings", this, false);
    }
    
    public boolean isIgnoreLocalSettings() {
        return Boolean.TRUE.equals(ignoreLocalSettings.getValue());
    }
    
    public static IgnoreLocalSettings getIgnoreLocalSettingsFromCurrentUrl() {
        return new SettingsToUrlSerializer().deserializeFromCurrentLocation(new IgnoreLocalSettings());
    }

}
