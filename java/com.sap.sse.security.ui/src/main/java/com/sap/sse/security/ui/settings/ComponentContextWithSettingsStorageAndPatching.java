package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.security.ui.client.UserService;

/**
 * Adds settings patching functionality to {@link ComponentContextWithSettingsStorage} implementation. This
 * implementation provides additional methods for attaching of {@link SettingsPatch}. There are multiple hooks in the
 * underlying {@link UserSettingsBuildingPipelineWithPatching} which can be used to influence the settings construction
 * in its construction pipeline. After each {@link PipelineLevel} a custom {@link SettingsPatch} may be applied. The
 * patch may partially or completely modify the resulting settings object in order to provide the desired behavior of
 * default settings for a dynamic environment, e.g. RaceBoard which determines its default settings regarding to
 * RaceModes, PlayModes and etc.
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
public class ComponentContextWithSettingsStorageAndPatching<S extends Settings>
        extends ComponentContextWithSettingsStorage<S> {

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public ComponentContextWithSettingsStorageAndPatching(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocator storageDefinition) {
        this(rootLifecycle, userService, storageDefinition, new SettingsRepresentationTransformer());
    }

    protected ComponentContextWithSettingsStorageAndPatching(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocator storageDefinition,
            SettingsRepresentationTransformer settingsSerializationHelper) {
        this(rootLifecycle, userService, storageDefinition, settingsSerializationHelper,
                new UserSettingsBuildingPipelineWithPatching(settingsSerializationHelper));
    }

    protected ComponentContextWithSettingsStorageAndPatching(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocator storageDefinition,
            SettingsRepresentationTransformer settingsSerializationHelper,
            UserSettingsBuildingPipelineWithPatching settingsBuildingPipeline) {
        super(rootLifecycle, userService, storageDefinition, settingsSerializationHelper, settingsBuildingPipeline);
    }

    /**
     * Adds a settings patch for transforming settings before storing them.
     * 
     * @param component
     *            The component which the targeted settings for patching belong to
     * @param pipelineLevel
     *            The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch
     *            The settings patch to apply on settings
     */
    public <CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel,
            SettingsPatch<CS> settingsPatch) {
        ((UserSettingsBuildingPipelineWithPatching) settingsBuildingPipeline).addPatchForStoringSettings(component,
                pipelineLevel, settingsPatch);
    }

    /**
     * Adds a settings patch for settings object construction.
     * 
     * @param component
     *            The component which the targeted settings for patching belong to
     * @param pipelineLevel
     *            The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param settingsPatch
     *            The settings patch to apply on settings
     */
    public <CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel,
            SettingsPatch<CS> settingsPatch) {
        ((UserSettingsBuildingPipelineWithPatching) settingsBuildingPipeline).addPatchForLoadingSettings(component,
                pipelineLevel, settingsPatch);
    }

}
