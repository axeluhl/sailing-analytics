package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.security.ui.client.UserService;

public class PlaceBasedUserSettingsStorageManager<S extends Settings> extends UserSettingsStorageManager<S> {

    public PlaceBasedUserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId, String serializedSettings) {
        super(userService, storageDefinitionId, new PlaceBasedUserSettingsBuildingPipeline(serializedSettings));
    }

}
