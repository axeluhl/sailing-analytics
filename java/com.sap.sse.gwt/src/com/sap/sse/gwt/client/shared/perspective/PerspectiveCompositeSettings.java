package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * A composite settings class for a perspective aggregating the {@link #getPerspectiveOwnSettings() settings of the
 * perspective itself} as well as the settings of all contained components. The perspective-specific settings are
 * <em>not</em> part of the general composite settings' {@link #getSettingsPerComponentId()} result (although a
 * {@link Perspective} is a {@link Component}) to make it clear that the perspective to which these settings belong is
 * not nested in itself. Note that any of the component of the component/settings pairs returned by
 * {@link #getSettingsPerComponentId()} may again be a perspective.
 * 
 * @author Frank Mittag
 *
 * @param <PS>
 *            the {@link Perspective}'s own settings type; those could, e.g., hold settings such as how much space
 *            is assigned to which component, or how the perspective displays itself, such as embedded or not
 */
public class PerspectiveCompositeSettings<PS extends Settings> extends CompositeSettings {
    private final PS perspectiveOwnSettings;
    
    public PerspectiveCompositeSettings(PS perspectiveOwnSettings, Map<String, Settings> settingsPerComponent) {
        super(settingsPerComponent);
        this.perspectiveOwnSettings = perspectiveOwnSettings;
    }

    /**
     * @return the perspective-specific settings; those could, e.g., be settings that govern which of the components are
     *         currently visible or how the components are to be displayed in the context of the perspective; these
     *         settings may also control visualization properties for the perspective that are not specific to any
     *         particular component, such as a background color, presence of a general header or general refresh
     *         behavior.
     */
    public PS getPerspectiveOwnSettings() {
        return perspectiveOwnSettings;
    }

    public boolean hasSettings() {
        return getSettingsPerComponentId() != null || perspectiveOwnSettings != null;
    }

    /**
     * These are the settings to be seen by serializer implementations through {@link SettingsMap} interface.
     * For the serialization, the perspectiveOwnSettings are included in addition to the child components' settings.
     * 
     * @see com.sap.sse.gwt.client.shared.components.CompositeSettings#getSettingsByKey()
     */
    @Override
    public Map<String, Settings> getSettingsPerComponentId() {
        Map<String, Settings> allSettings;
        if(perspectiveOwnSettings == null) {
            allSettings = super.getSettingsPerComponentId();
        } else {
            allSettings = new HashMap<>(super.getSettingsPerComponentId());
            allSettings.put(null, perspectiveOwnSettings);
        }
        return allSettings;
    }
}
