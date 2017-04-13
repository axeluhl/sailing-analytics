package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.SettingsStringConverter;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

public class PlaceBasedUserSettingsBuildingPipeline extends UserSettingsBuildingPipeline {
    
    public PlaceBasedUserSettingsBuildingPipeline(final String serializedSettings) {
        super(new SettingsStringConverter() {
            
            private final SettingsToStringSerializer settingsToStringSerializer = new SettingsToStringSerializer();
            
            @SuppressWarnings("unchecked")
            @Override
            public <S extends Settings> S deserializeFromCurrentUrl(S defaultSettings) {
                final S result;
                if (defaultSettings instanceof SettingsMap) {
                    result = (S) settingsToStringSerializer.fromString(serializedSettings, (SettingsMap) defaultSettings);
                } else if (defaultSettings instanceof GenericSerializableSettings) {
                    result = (S) settingsToStringSerializer.fromString(serializedSettings,
                            (GenericSerializableSettings) defaultSettings);
                } else {
                    result = defaultSettings;
                }
                return result;
            }
            
        });
    }

}
