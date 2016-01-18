package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.settings.Settings;


public class CompositeLifecycle {
    public static class ComponentConstructionParameters<C extends Component<SettingsType>, SettingsType extends Settings> {
        private final ComponentLifecycle<C, SettingsType, ?,?> componentLifecycle;
        private final ComponentConstructorArgs<C, SettingsType> componentConstructorArgs; 
        private final SettingsType settings;

        public ComponentConstructionParameters(ComponentLifecycle<C, SettingsType, ?,?> componentLifecycle,
                ComponentConstructorArgs<C, SettingsType> componentConstructorArgs,
                SettingsType settings) {
            this.componentLifecycle = componentLifecycle;
            this.componentConstructorArgs = componentConstructorArgs;
            this.settings = settings;
        }

        public ComponentLifecycle<?, SettingsType, ?,?> getComponentLifecycle() {
            return componentLifecycle;
        }
        
        public SettingsType getSettings() {
            return settings;
        }

        public ComponentConstructorArgs<C, SettingsType> getComponentConstructorArgs() {
            return componentConstructorArgs;
        }
    }
    
    private final Iterable<ComponentConstructionParameters<?,?>> componentLifecycles;

    public CompositeLifecycle(Iterable<ComponentConstructionParameters<?, ?>> componentLifecycles) {
        this.componentLifecycles = componentLifecycles;
    }

    public Iterable<ComponentConstructionParameters<?, ?>> getComponentLifecycles() {
        return componentLifecycles;
    }
}
