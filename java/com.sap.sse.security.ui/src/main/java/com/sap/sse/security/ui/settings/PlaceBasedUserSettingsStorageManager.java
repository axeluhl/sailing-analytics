package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.security.ui.client.UserService;

/**
 * Specialization of {@link UserSettingsStorageManager} which uses {@link PlaceBasedUserSettingsBuildingPipeline} for settings construction.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class PlaceBasedUserSettingsStorageManager<S extends Settings> extends UserSettingsStorageManager<S> {

    public PlaceBasedUserSettingsStorageManager(UserService userService, StorageDefinitionId storageDefinitionId, String serializedSettings) {
        super(userService, storageDefinitionId, new PlaceBasedUserSettingsBuildingPipeline(serializedSettings));
    }

}
