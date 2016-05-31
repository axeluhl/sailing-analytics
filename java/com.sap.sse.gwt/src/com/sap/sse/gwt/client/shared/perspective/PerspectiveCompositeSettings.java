package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentIdAndSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * A composite settings class for a perspective aggregating the {@link #getPerspectiveSettings() settings of the
 * perspective itself} as well as the settings of all contained components. The perspective-specific settings are
 * <em>not</em> part of the general composite settings' {@link #getSettingsPerComponentId()} result (although a
 * {@link Perspective} is a {@link Component}) to make it clear that the perspective to which these settings belong is
 * not nested in itself. Note that any of the component of the component/settings pairs returned by
 * {@link #getSettingsPerComponentId()} may again be a perspective, but not the perspective returned by
 * {@link #getPerspectiveAndSettings()}.{@link PerspectiveIdAndSettings#getPerspective() getPerspective()}.
 * 
 * @author Frank Mittag
 *
 * @param <PS>
 *            the {@link Perspective} settings type
 */
public class PerspectiveCompositeSettings<PS extends Settings> extends CompositeSettings {
    private final PerspectiveIdAndSettings<PS> perspectiveAndSettings;
    
    public PerspectiveCompositeSettings(PerspectiveIdAndSettings<PS> perspectiveAndSettings, Iterable<ComponentIdAndSettings<?>> settingsPerComponent) {
        super(settingsPerComponent);
        this.perspectiveAndSettings = perspectiveAndSettings;
    }

    /**
     * @return the perspective-specific settings; those could, e.g., be settings that govern which of the components are
     *         currently visible or how the components are to be displayed in the context of the perspective; these
     *         settings may also control visualization properties for the perspective that are not specific to any
     *         particular component, such as a background color, presence of a general header or general refresh
     *         behavior.
     */
    public PS getPerspectiveSettings() {
        return perspectiveAndSettings.getSettings();
    }

    public PerspectiveIdAndSettings<PS> getPerspectiveAndSettings() {
        return perspectiveAndSettings;
    }
}
