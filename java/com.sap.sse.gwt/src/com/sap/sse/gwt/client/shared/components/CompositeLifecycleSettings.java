package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;

public class CompositeLifecycleSettings extends AbstractSettings {
    private final Iterable<ComponentLifecycleAndSettings<?>> settingsPerComponentLifecycle;

    public CompositeLifecycleSettings(Iterable<ComponentLifecycleAndSettings<?>> settingsPerComponent) {
        this.settingsPerComponentLifecycle = settingsPerComponent;
    }

    public Iterable<ComponentLifecycleAndSettings<?>> getSettingsPerComponentLifecycle() {
        return settingsPerComponentLifecycle;
    }
}
