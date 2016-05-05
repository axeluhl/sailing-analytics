package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;

public class CompositeLifecycleSettings extends AbstractSettings {
    private final Iterable<ComponentLifecycleAndSettings<?,?>> settingsPerComponentLifecycle;

    public CompositeLifecycleSettings(Iterable<ComponentLifecycleAndSettings<?,?>> settingsPerComponentLifecycle) {
        this.settingsPerComponentLifecycle = settingsPerComponentLifecycle;
    }

    public Iterable<ComponentLifecycleAndSettings<?,?>> getSettingsPerComponentLifecycle() {
        return settingsPerComponentLifecycle;
    }

    public <S extends Settings> S getSettingsOfComponentLifecycle(ComponentLifecycle<S,?> componentLifecycle) {
        S result = null;
        ComponentLifecycleAndSettings<ComponentLifecycle<S,?>, S> componentLifecycleAndSettings = findComponentLifecycleAndSettings(componentLifecycle);
        if (componentLifecycleAndSettings != null) {
            result = componentLifecycleAndSettings.getSettings();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <CL extends ComponentLifecycle<S,?>, S extends Settings> ComponentLifecycleAndSettings<CL,S> findComponentLifecycleAndSettings(ComponentLifecycle<S,?> componentLifecycle) {
        ComponentLifecycleAndSettings<CL,S> result = null;
        for (ComponentLifecycleAndSettings<?,?> componentLifecycleAndSettings : settingsPerComponentLifecycle) {
            if (componentLifecycleAndSettings.getComponentLifecycle() == componentLifecycle) {
                result = (ComponentLifecycleAndSettings<CL, S>) componentLifecycleAndSettings;
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
