package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettings;

public class PerspectiveCompositeSettings extends AbstractSettings {
    private final Iterable<ComponentAndSettings<?>> settingsPerComponent;
    private final PerspectiveAndSettingsPair<?> perspectiveSettings;
    
    public PerspectiveCompositeSettings(PerspectiveAndSettingsPair<?> perspectiveSettings, Iterable<ComponentAndSettings<?>> settingsPerComponent) {
        this.settingsPerComponent = settingsPerComponent;
        this.perspectiveSettings = perspectiveSettings;
    }

    public Iterable<ComponentAndSettings<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }

    public PerspectiveAndSettingsPair<?> getPerspectiveSettings() {
        return perspectiveSettings;
    }
}
