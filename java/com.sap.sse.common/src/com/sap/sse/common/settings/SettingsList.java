package com.sap.sse.common.settings;

import com.sap.sse.common.settings.base.AbstractListSetting;


public class SettingsList<T extends Settings> extends AbstractListSetting<T> implements SettingsListSetting<T> {
    
    private SettingsFactory<T> settingsFactory;
    
    public SettingsList(String name, AbstractSettings settings, SettingsFactory<T> settingsFactory) {
        super(name, settings);
        this.settingsFactory = settingsFactory;
    }

    @Override
    public SettingsFactory<T> getSettingsFactory() {
        return settingsFactory;
    }
    
    @Override
    public String toString() {
        return getValues().toString();
    }
}
