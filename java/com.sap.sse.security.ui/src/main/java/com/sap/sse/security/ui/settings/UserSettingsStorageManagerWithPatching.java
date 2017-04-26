package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.security.ui.client.UserService;

/**
 * Specialization of {@link UserSettingsStorageManager} which uses {@link UserSettingsBuildingPipelineWithPatching} for settings construction.
 * This implementation provides additional methods for attaching of {@link SettingsPatch}. There are multiple hooks in
 * the underlying {@link UserSettingsBuildingPipelineWithPatching} which can be used to influence the
 * settings construction in its construction pipeline. After each {@link PipelineLevel} a custom {@link SettingsPatch}
 * may be applied. The patch may partially or completely modify the resulting settings object in order to provide
 * the desired behavior of default settings for a dynamic environment, e.g. RaceBoard which determines its default settings regarding
 * to RaceModes, PlayModes and etc.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class UserSettingsStorageManagerWithPatching<S extends Settings> extends UserSettingsStorageManager<S> {
    
    /**
     * The typed pipeline used for the settings construction.
     */
    protected final UserSettingsBuildingPipelineWithPatching settingsBuildingPipeline;

    /**
     * Constructs the instance with {@link UserSettingsBuildingPipelineWithPatching}.
     * 
     * @param userService The service which is used for server-side settings storage
     * @param storageDefinitionId The definition for User Settings and Document Settings storage keys
     */
    public UserSettingsStorageManagerWithPatching(UserService userService, StorageDefinitionId storageDefinitionId) {
        super(userService, storageDefinitionId, new UserSettingsBuildingPipelineWithPatching());
        settingsBuildingPipeline = (UserSettingsBuildingPipelineWithPatching) super.settingsBuildingPipeline;
    }
    
    /**
     * Adds a settings patch for transforming settings before storing them.
     * 
     * @param component The component which the targeted settings for patching belong to
     * @param pipelineLevel The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch The settings patch to apply on settings
     */
    public<CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsBuildingPipeline.addPatchForStoringSettings(component, pipelineLevel, settingsPatch);
    }
    
    /**
     * Adds a settings patch for settings object construction.
     * 
     * @param component The component which the targeted settings for patching belong to
     * @param pipelineLevel The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch The settings patch to apply on settings
     */
    public<CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsBuildingPipeline.addPatchForLoadingSettings(component, pipelineLevel, settingsPatch);
    }
    
}
