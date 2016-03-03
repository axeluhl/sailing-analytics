package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.gwt.client.shared.components.ComponentAndSettingsPair;

public class PerspectiveCompositeSettings extends AbstractSettings {
    private final Iterable<ComponentAndSettingsPair<?>> settingsPerComponent;
    private final PerspectiveAndSettingsPair<?> perspectiveSettings;
    
    public PerspectiveCompositeSettings(PerspectiveAndSettingsPair<?> perspectiveSettings, Iterable<ComponentAndSettingsPair<?>> settingsPerComponent) {
        this.settingsPerComponent = settingsPerComponent;
        this.perspectiveSettings = perspectiveSettings;
    }

    public Iterable<ComponentAndSettingsPair<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }

    public PerspectiveAndSettingsPair<?> getPerspectiveSettings() {
        return perspectiveSettings;
    }
}
