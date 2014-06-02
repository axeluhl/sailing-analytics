package com.sap.sailing.gwt.ui.client.shared.components;

import com.sap.sse.common.Util;

public class CompositeSettings {
    public static class ComponentAndSettingsPair<SettingsType> extends Util.Pair<Component<SettingsType>, SettingsType> {
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
