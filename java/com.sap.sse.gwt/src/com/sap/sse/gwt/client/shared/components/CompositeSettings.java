package com.sap.sse.gwt.client.shared.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * Groups settings for multiple {@link Component}s. This can be of particular interest when working with
 * {@link Perspective}s and the perspective's {@link PerspectiveCompositeSettings composite settings}.
 */
public class CompositeSettings extends AbstractSettings implements SettingsMap {
    private final Map<String, Settings> settingsPerComponentId;

    public CompositeSettings(Map<String, Settings> settingsPerComponentId) {
        this.settingsPerComponentId = new HashMap<>(settingsPerComponentId);
    }
    
    @Override
    public Map<String, Settings> getSettingsPerComponentId() {
        return Collections.unmodifiableMap(settingsPerComponentId);
    }
    
    @SuppressWarnings("unchecked")
    public <S extends Settings> S findSettingsByComponentId(String componentId) {
        return (S) settingsPerComponentId.get(componentId);
    }
}
