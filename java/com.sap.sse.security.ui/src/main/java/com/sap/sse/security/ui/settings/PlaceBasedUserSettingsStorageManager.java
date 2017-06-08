package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.settings.SettingsToStringSerializer;
import com.sap.sse.security.ui.client.UserService;

public class PlaceBasedUserSettingsStorageManager<S extends Settings> extends UserSettingsStorageManager<S> {

    private final SettingsToStringSerializer settingsToStringSerializer = new SettingsToStringSerializer();
    private final String serializedSettings;

    public PlaceBasedUserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId, String serializedSettings) {
        super(userService, storageDefinitionId);
        this.serializedSettings = serializedSettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected S deserializeFromCurrentUrl(S defaultSettings) {
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
}
