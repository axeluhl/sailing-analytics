package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;

public class CompositeLifecycleSettings extends AbstractSettings {
    private final Iterable<ComponentLifecycleAndSettings<?,?>> settingsPerComponentLifecycle;

    public CompositeLifecycleSettings(Iterable<ComponentLifecycleAndSettings<?,?>> settingsPerComponent) {
        this.settingsPerComponentLifecycle = settingsPerComponent;
    }

    public Iterable<ComponentLifecycleAndSettings<?,?>> getSettingsPerComponentLifecycle() {
        return settingsPerComponentLifecycle;
    }

    public <S extends Settings> S getSettingsOfComponentLifecycle(ComponentLifecycle<?,S,?> componentLifecycle) {
        S result = null;
        ComponentLifecycleAndSettings<ComponentLifecycle<?,S,?>, S> componentLifecycleAndSettings = findComponentLifecycleAndSettings(componentLifecycle);
        if (componentLifecycleAndSettings != null) {
            result = componentLifecycleAndSettings.getSettings();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <C extends ComponentLifecycle<?,S,?>, S extends Settings> ComponentLifecycleAndSettings<C,S> findComponentLifecycleAndSettings(ComponentLifecycle<?,S,?> componentLifecycle) {
        ComponentLifecycleAndSettings<C,S> result = null;
        for (ComponentLifecycleAndSettings<?,?> componentLifecycleAndSettings : settingsPerComponentLifecycle) {
            if (componentLifecycleAndSettings.getComponentLifecycle() == componentLifecycle) {
                result = (ComponentLifecycleAndSettings<C, S>) componentLifecycleAndSettings;
                break;
            }
        }
        return result;
    }

    public boolean hasSettings() {
        boolean result = false;
        for (ComponentLifecycleAndSettings<?,?> componentLifecycleAndSettings : settingsPerComponentLifecycle) {
            if (componentLifecycleAndSettings.getComponentLifecycle().hasSettings()) {
                result = true;
                break;
            }
        }
        return result;
    }    
}
