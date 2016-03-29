package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;

public class CompositeSettings extends AbstractSettings {
    private final Iterable<ComponentAndSettings<?>> settingsPerComponent;

    public CompositeSettings(Iterable<ComponentAndSettings<?>> settingsPerComponent) {
        this.settingsPerComponent = settingsPerComponent;
    }

    public Iterable<ComponentAndSettings<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }
}
