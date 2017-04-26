package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.Settings;

public abstract class AbstractSettingsBuildingPipeline implements SettingsBuildingPipeline {
    
    protected final SettingsStringConverter settingsStringConverter;
    
    public AbstractSettingsBuildingPipeline() {
        this(new SettingsStringConverter());
    }
    
    public AbstractSettingsBuildingPipeline(SettingsStringConverter settingsStringConverter) {
        this.settingsStringConverter = settingsStringConverter;
    }

    @Override
    public<S extends Settings> S getSettingsObject(S defaultSettings, SettingsStrings settingsStrings) {
        SettingsJsons settingsJsons = settingsStringConverter.convertToSettingsJson(settingsStrings);
        return getSettingsObject(defaultSettings, settingsJsons);
    }
    
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings) {
        return getSettingsObject(defaultSettings, new SettingsJsons(null, null));
    }
    
    public SettingsStringConverter getSettingsStringConverter() {
        return settingsStringConverter;
    }

}
