package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;

public class CompositeLifecycleSettings extends AbstractSettings {
    private final Iterable<ComponentLifecycleAndSettings<?>> settingsPerComponentLifecycle;

    public CompositeLifecycleSettings(Iterable<ComponentLifecycleAndSettings<?>> settingsPerComponent) {
        this.settingsPerComponentLifecycle = settingsPerComponent;
    }

    public Iterable<ComponentLifecycleAndSettings<?>> getSettingsPerComponentLifecycle() {
        return settingsPerComponentLifecycle;
    }

    public <S extends Settings> S getSettingsOfComponentLifecycle(ComponentLifecycle<?,S,?,?> componentLifecycle) {
        S result = null;
        for (ComponentLifecycleAndSettings<?> componentLifecycleAndSettings: settingsPerComponentLifecycle) {
            if (componentLifecycleAndSettings.getComponentLifecycle() == componentLifecycle) {
                result = (S) componentLifecycleAndSettings.getSettings();
                break;
            }
        }
        return result;
    }

    public boolean hasSettings() {
        boolean result = false;
        for (ComponentLifecycleAndSettings<?> componentLifecycleAndSettings: settingsPerComponentLifecycle) {
            if (componentLifecycleAndSettings.getComponentLifecycle().hasSettings()) {
                result = true;
                break;
            }
        }
        return result;
    }    
}
