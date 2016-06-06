package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

/**
 * Groups settings for multiple {@link Component}s. This can be of particular interest when working with
 * {@link Perspective}s and the perspective's {@link PerspectiveCompositeSettings composite settings}.
 */
public class CompositeSettings extends AbstractSettings {
    private final Map<Serializable, Settings> settingsPerComponentId;

    public CompositeSettings(Map<Serializable, Settings> settingsPerComponentId) {
        this.settingsPerComponentId = new HashMap<>(settingsPerComponentId);
    }

    public Map<Serializable, Settings> getSettingsPerComponentId() {
        return Collections.unmodifiableMap(settingsPerComponentId);
    }
    
    public <C extends ComponentLifecycle<S,?>, S extends Settings> Settings findSettingsByComponentId(Serializable componentId) {
        return settingsPerComponentId.get(componentId);
    }
}
