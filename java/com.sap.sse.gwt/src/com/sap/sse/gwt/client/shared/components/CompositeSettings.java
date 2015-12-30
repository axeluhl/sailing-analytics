package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;

public class CompositeSettings extends AbstractSettings {
    // TODO: Remove after lifecycle fix of datamining
    public static class ComponentAndSettingsPair<SettingsType extends Settings> extends Util.Pair<Component<SettingsType>, SettingsType> {
        private static final long serialVersionUID = -569811233041583043L;

        public ComponentAndSettingsPair(Component<SettingsType> a, SettingsType b) {
            super(a, b);
        }
    }
    
    public static class ComponentLifecycleAndSettingsPair<SettingsType extends Settings> {
        private final ComponentLifecycle<?, SettingsType, ?> componentLifecycle;
        private final SettingsType settings;

        public ComponentLifecycleAndSettingsPair(ComponentLifecycle<?, SettingsType, ?> componentLifecycle,
                SettingsType settings) {
            super();
            this.componentLifecycle = componentLifecycle;
            this.settings = settings;
        }

        public ComponentLifecycle<?, SettingsType, ?> getA() {
            return componentLifecycle;
        }
        
        public SettingsType getB() {
            return settings;
        }
    }
    
    private final Iterable<ComponentLifecycleAndSettingsPair<?>> settingsPerComponentLifecycle;
    private final Iterable<ComponentAndSettingsPair<?>> settingsPerComponent;

    // TODO: Remove after lifecycle fix of datamining
    public CompositeSettings(Iterable<ComponentAndSettingsPair<?>> settingsPerComponent, String x) {
        this.settingsPerComponentLifecycle = null;
        this.settingsPerComponent = settingsPerComponent;
    }

    public CompositeSettings(Iterable<ComponentLifecycleAndSettingsPair<?>> settingsPerComponent) {
        this.settingsPerComponentLifecycle = settingsPerComponent;
        this.settingsPerComponent = null;
    }

    public Iterable<ComponentAndSettingsPair<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }

    public Iterable<ComponentLifecycleAndSettingsPair<?>> getSettingsPerComponentLifecycle() {
        return settingsPerComponentLifecycle;
    }

    @SuppressWarnings("unchecked")
    public <SettingsType extends Settings> SettingsType getSettingsForType(Class<SettingsType> settingsType) {
        SettingsType result = null;
        for(ComponentLifecycleAndSettingsPair<?> componentAndSettings: settingsPerComponentLifecycle) {
            if(componentAndSettings.getB().getClass().getName().equals(settingsType.getName())) {
                result = (SettingsType) componentAndSettings.getB();
                break;
            }
        }
        return result;
    }
}
