package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;
import com.sap.sse.gwt.client.shared.components.CompositeSettings;

/**
 * A composite settings class for a perspective aggregating the settings of the perspective itself as well
 * as the settings of all contained components.
 * @author Frank
 *
 * @param <PS>
 *      the {@link Perspective} settings type
 */
public class PerspectiveCompositeSettings<PS extends Settings> extends CompositeSettings {
    private final ComponentAndSettings<PS> perspectiveAndSettings;
    
    public PerspectiveCompositeSettings(ComponentAndSettings<PS> perspectiveAndSettings, Iterable<ComponentAndSettings<?>> settingsPerComponent) {
        super(settingsPerComponent);
        
        this.perspectiveAndSettings = perspectiveAndSettings;
    }

    public PS getPerspectiveSettings() {
        return perspectiveAndSettings.getSettings();
    }
}
