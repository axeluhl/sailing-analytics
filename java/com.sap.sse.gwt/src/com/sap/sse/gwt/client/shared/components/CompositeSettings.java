package com.sap.sse.gwt.client.shared.components;

import com.sap.sse.common.Util;
import com.sap.sse.common.settings.AbstractSettings;
import com.sap.sse.common.settings.Settings;

public class CompositeSettings extends AbstractSettings {
    public static class ComponentAndSettingsPair<SettingsType extends Settings> extends Util.Pair<Component<SettingsType>, SettingsType> {
        private static final long serialVersionUID = -569811233041583043L;

        public ComponentAndSettingsPair(Component<SettingsType> a, SettingsType b) {
            super(a, b);
        }
    }
    
    private final Iterable<ComponentAndSettingsPair<?>> settingsPerComponent;

    public CompositeSettings(Iterable<ComponentAndSettingsPair<?>> settingsPerComponent) {
        this.settingsPerComponent = settingsPerComponent;
    }

    public Iterable<ComponentAndSettingsPair<?>> getSettingsPerComponent() {
        return settingsPerComponent;
    }

}
