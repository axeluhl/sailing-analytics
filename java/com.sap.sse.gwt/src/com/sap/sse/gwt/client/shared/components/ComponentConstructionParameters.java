package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;

public class ComponentConstructionParameters<C extends Component<SettingsType>, SettingsType extends Settings, SDC extends SettingsDialogComponent<SettingsType>, CCA extends ComponentConstructorArgs<C, SettingsType>> {
    private final ComponentLifecycle<C, SettingsType, SDC, CCA> componentLifecycle;
    private final CCA componentConstructorArgs; 
    private final SettingsType settings;

    public ComponentConstructionParameters(ComponentLifecycle<C, SettingsType, SDC, CCA> componentLifecycle,
            CCA componentConstructorArgs, SettingsType settings) {
        this.componentLifecycle = componentLifecycle;
        this.componentConstructorArgs = componentConstructorArgs;
        this.settings = settings;
    }

    public ComponentLifecycle<C, SettingsType, SDC, CCA> getComponentLifecycle() {
        return componentLifecycle;
    }
    
    public SettingsType getSettings() {
        return settings;
    }

    public ComponentConstructorArgs<C, SettingsType> getComponentConstructorArgs() {
        return componentConstructorArgs;
    }
    
    public C createComponent() {
        return componentLifecycle.createComponent(componentConstructorArgs, settings);
    }
}