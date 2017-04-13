package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.PipelineLevel;
import com.sap.sse.security.ui.client.UserService;

public class UserSettingsStorageManagerWithPatching<S extends Settings> extends UserSettingsStorageManager<S> {
    
    private final UserSettingsBuildingPipelineWithPatching settingsBuildingPipeline;

    public UserSettingsStorageManagerWithPatching(UserService userService, StorageDefinitionId storageDefinitionId) {
        super(userService, storageDefinitionId, new UserSettingsBuildingPipelineWithPatching());
        settingsBuildingPipeline = (UserSettingsBuildingPipelineWithPatching) super.settingsBuildingPipeline;
    }
    
    public<CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsBuildingPipeline.addPatchForStoringSettings(component, pipelineLevel, settingsPatch);
    }
    
    public<CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsBuildingPipeline.addPatchForLoadingSettings(component, pipelineLevel, settingsPatch);
    }
    
}
