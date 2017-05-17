package com.sap.sse.security.ui.settings;

import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;

/**
 * Specialization of {@link UserSettingsBuildingPipeline} which offers multiple hooks for settings patching throughout
 * the pipeline. These hooks may be used to patch the settings at different pipeline levels during its construction.
 * This implementation provides additional methods for attachment of {@link SettingsPatch}s. After each
 * {@link PipelineLevel} a custom {@link SettingsPatch} may be applied. The patch may partially or completely modify the
 * resulting settings object in order to provide the desired behavior of default settings for a dynamic environment,
 * e.g. RaceBoard which determines its default settings regarding to RaceModes, PlayModes and etc.
 * 
 * @author Vladislav Chumak
 *
 */
public class UserSettingsBuildingPipelineWithAdditionalSettingsLayers extends UserSettingsBuildingPipelineWithPatching {
    
    private SettingsPatches layersSettingsPatches = new SettingsPatches();

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its storable representation.
     * 
     * @param settingsRepresentationTransformer
     *            The custom conversion helper
     */
    public UserSettingsBuildingPipelineWithAdditionalSettingsLayers(
            SettingsRepresentationTransformer settingsRepresentationTransformer) {
        super(settingsRepresentationTransformer);
    }

    /**
     * Converts the provided settings object into a storable settings representation without considering provided
     * pipeline level and settings tree path.
     * 
     * @param settings
     *            The settings to convert to storable settings representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The path of the settings in the settings tree
     * @return The storable settings representation of the provided settings
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfUserSettings(CS newSettings,
            CS newInstance, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation,
            List<String> path) {
        CS pipelinedSettings = SettingsUtil.copyDefaultsFromValues(newInstance, newInstance);
        pipelinedSettings = SettingsUtil.copyDefaults(newSettings, newInstance); // overrides values which are set to
                                                                                 // default values
        pipelinedSettings = applyPatchesForPipelineLevel(pipelinedSettings, PipelineLevel.SYSTEM_DEFAULTS, path,
                layersSettingsPatches);
        pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);

        SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);
        SettingsUtil.copyValues(newSettings, pipelinedSettings);

        return settingsRepresentationTransformer.convertToSettingsRepresentation(pipelinedSettings);
    }


    /**
     * Adds a settings patch for transforming settings before storing them.
     * 
     * @param component
     *            The component which the targeted settings for patching belong to
     * @param afterLevel
     *            The pipeline level <b>AFTER</b> that the patch should be applied on settings
     * @param additionalLayerSettings
     *            The settings patch to apply on settings
     */
    public <CS extends GenericSerializableSettings> void addAdditionalSettingsLayer(Component<CS> component, PipelineLevel afterLevel,
            CS additionalLayerSettings) {
        layersSettingsPatches.addSettingsPatch(component.getPath(), new AdditionalSettingsLayer<Settings>(additionalLayerSettings, settingsRepresentationTransformer), afterLevel);
    }

}