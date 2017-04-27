package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.settings.SettingsStringConverter;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;

/**
 * Specialization of {@link UserSettingsBuildingPipeline} which reimplements URL deserialization.
 * This implementation handles URL deserialization in such a way, that only the provided
 * {@code serializedSettings} {@code String} is considered as URL, and not the URL in the browser.
 * 
 * @author Vladislav Chumak
 * 
 * @see UserSettingsBuildingPipeline
 *
 */
public class PlaceBasedUserSettingsBuildingPipeline extends UserSettingsBuildingPipeline {
    
    /**
     * Constructs an instance with a custom conversion helper which considers the provided {@code serializedSettings}
     * as URL parameters for deserialization of URL Settings.
     * 
     * @param serializedSettings
     */
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
