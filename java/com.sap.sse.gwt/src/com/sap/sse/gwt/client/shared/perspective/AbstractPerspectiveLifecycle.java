package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycleAndSettings;
import com.sap.sse.gwt.client.shared.components.CompositeLifecycleSettings;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

/**
 * An abstract base class for perspective lifecycles.
 * @author Frank
 *
 */
public abstract class AbstractPerspectiveLifecycle<P extends Perspective<SettingsType>, SettingsType extends Settings, SDP extends SettingsDialogComponent<SettingsType>>
    implements PerspectiveLifecycle<P, SettingsType, SDP> {

    protected final List<ComponentLifecycle<?,?,?>> componentLifecycles;
    
    public AbstractPerspectiveLifecycle() {
        componentLifecycles = new ArrayList<>();
    }
    
    @Override
    public CompositeLifecycleSettings getComponentLifecyclesAndDefaultSettings() {
        List<ComponentLifecycleAndSettings<?,?>> lifecyclesAndSettings = new ArrayList<>();
        for(ComponentLifecycle<?,?,?> componentLifecycle: componentLifecycles) {
            lifecyclesAndSettings.add(createComponentLifecycleAndSettings(componentLifecycle));
        }
        CompositeLifecycleSettings compositeSettings = new CompositeLifecycleSettings(lifecyclesAndSettings);
        return compositeSettings;
    }

    private <C extends ComponentLifecycle<?,S,?>, S extends Settings> ComponentLifecycleAndSettings<C,S> createComponentLifecycleAndSettings(C componentLifecycle) {
        S defaultSettings = componentLifecycle.createDefaultSettings();
        ComponentLifecycleAndSettings<C,S> componentLifecycleAndSettings = new ComponentLifecycleAndSettings<>(componentLifecycle, defaultSettings);
        return componentLifecycleAndSettings;
    }
    
    public Iterable<ComponentLifecycle<?,?,?>> getComponentLifecycles() {
        return componentLifecycles;
    }

    @Override
    public String getPerspectiveName() {
        return getLocalizedShortName();
    }
}
