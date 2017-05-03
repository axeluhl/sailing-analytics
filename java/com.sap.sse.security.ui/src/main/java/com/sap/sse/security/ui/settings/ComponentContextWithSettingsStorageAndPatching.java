package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;

/**
 * Adds settings patching functionality to {@link ComponentContextWithSettingsStorage} implementation.
 * This implementation provides additional methods for attaching of {@link SettingsPatch}. There are multiple hooks in
 * the underlying {@link UserSettingsBuildingPipelineWithPatching} which can be used to influence the
 * settings construction in its construction pipeline. After each {@link PipelineLevel} a custom {@link SettingsPatch}
 * may be applied. The patch may partially or completely modify the resulting settings object in order to provide
 * the desired behavior of default settings for a dynamic environment, e.g. RaceBoard which determines its default settings regarding
 * to RaceModes, PlayModes and etc.
 * 
 * 
 * @author Vladislav Chumak
 * @see ComponentContextWithSettingsStorage
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 *            
 */
public class ComponentContextWithSettingsStorageAndPatching<S extends Settings> extends ComponentContextWithSettingsStorage<S> {
    
    /**
     * Manages the persistence layer of settings.
     */
    protected final UserSettingsStorageManagerWithPatching<S> settingsStorageManager;
    
    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param settingsStorageManager
     *            The {@link UserSettingsStorageManagerWithPatching} to be used to access stored settings and to store new settings
     */
    public ComponentContextWithSettingsStorageAndPatching(ComponentLifecycle<S> rootLifecycle, UserSettingsStorageManagerWithPatching<S> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
        this.settingsStorageManager = (UserSettingsStorageManagerWithPatching<S>) super.settingsStorageManager;
    }
    
    /**
     * Adds a settings patch for transforming settings before storing them.
     * 
     * @param component The component which the targeted settings for patching belong to
     * @param pipelineLevel The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch The settings patch to apply on settings
     */
    public<CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsStorageManager.addPatchForStoringSettings(component, pipelineLevel, settingsPatch);
    }
    
    /**
     * Adds a settings patch for settings object construction.
     * 
     * @param component The component which the targeted settings for patching belong to
     * @param pipelineLevel The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch The settings patch to apply on settings
     */
    public<CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        invalidateCachedSettings();
        settingsStorageManager.addPatchForLoadingSettings(component, pipelineLevel, settingsPatch);
    }

}
