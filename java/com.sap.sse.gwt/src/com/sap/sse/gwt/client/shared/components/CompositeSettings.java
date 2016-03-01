package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;

public class CompositeSettings extends AbstractSettings {
    private final Iterable<ComponentAndSettingsPair<?>> settingsPerComponent;

    public CompositeSettings(Iterable<ComponentAndSettingsPair<?>> settingsPerComponent) {
        this.settingsPerComponent = settingsPerComponent;
    }

    public Iterable<ComponentAndSettingsPair<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }
}
