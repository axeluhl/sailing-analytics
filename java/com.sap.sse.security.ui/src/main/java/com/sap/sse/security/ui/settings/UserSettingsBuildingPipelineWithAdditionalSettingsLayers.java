package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
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
public class UserSettingsBuildingPipelineWithAdditionalSettingsLayers extends UserSettingsBuildingPipeline {
    
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
    
    @Override
    public <CS extends Settings> CS getSettingsObject(CS systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentations,
            List<String> absolutePathOfComponentWithSettings) {
        CS effectiveSettings = applyPatchesForPipelineLevel(systemDefaultSettings, PipelineLevel.SYSTEM_DEFAULTS,
                absolutePathOfComponentWithSettings, layersSettingsPatches);
        if (settingsRepresentations.hasStoredUserSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    effectiveSettings, settingsRepresentations.getUserSettingsRepresentation());
        }
        effectiveSettings = applyPatchesForPipelineLevel(effectiveSettings, PipelineLevel.USER_DEFAULTS,
                absolutePathOfComponentWithSettings, layersSettingsPatches);
        if (settingsRepresentations.hasStoredDocumentSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    effectiveSettings, settingsRepresentations.getDocumentSettingsRepresentation());
        }

        effectiveSettings = applyPatchesForPipelineLevel(effectiveSettings, PipelineLevel.DOCUMENT_DEFAULTS,
                absolutePathOfComponentWithSettings, layersSettingsPatches);
        effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithUrlSettings(effectiveSettings);
        return effectiveSettings;
    }

    protected static <CS extends Settings> CS applyPatchesForPipelineLevel(CS currentSettings,
            PipelineLevel pipelineLevel, List<String> absolutePathOfComponentWithSettings,
            SettingsPatches settingsPatchesToConsider) {
        CS effectiveSettings = currentSettings;
        for (Entry<List<String>, List<SettingsPatch<? extends Settings>>> entry : settingsPatchesToConsider
                .getSettingsPatches(pipelineLevel).entrySet()) {
            List<String> path = new ArrayList<>(entry.getKey());
            if (path.size() >= absolutePathOfComponentWithSettings.size()
                    && path.subList(path.size() - absolutePathOfComponentWithSettings.size(), path.size())
                            .equals(absolutePathOfComponentWithSettings)) {
                path = path.subList(0, path.size() - absolutePathOfComponentWithSettings.size());
                List<SettingsPatch<? extends Settings>> settingsPatches = entry.getValue();
                if (!settingsPatches.isEmpty()) {
                    Settings patchedComponentSettings = ComponentUtils
                            .determineComponentSettingsFromPerspectiveSettings(new ArrayList<>(path),
                                    effectiveSettings);
                    for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
                        patchedComponentSettings = patchSettings(patchedComponentSettings, settingsPatch);
                    }
                    effectiveSettings = ComponentUtils.patchSettingsTree(new ArrayList<>(path),
                            patchedComponentSettings, effectiveSettings);
                }
            }
        }
        return effectiveSettings;
    }
    
    @SuppressWarnings("unchecked")
    private static <CS extends Settings> CS patchSettings(Settings settings, SettingsPatch<CS> settingsPatch) {
        return settingsPatch.patchSettings((CS) settings);
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

        pipelinedSettings = SettingsUtil.copyValues(newSettings, pipelinedSettings);

        return settingsRepresentationTransformer.convertToSettingsRepresentation(pipelinedSettings);
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
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfDocumentSettings(
            CS newSettings, CS newInstance,
            StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, List<String> path) {
        CS pipelinedSettings = SettingsUtil.copyDefaultsFromValues(newInstance, newInstance);
        pipelinedSettings = SettingsUtil.copyDefaults(newSettings, newInstance); // overrides values which are set to
                                                                                 // default values
        pipelinedSettings = applyPatchesForPipelineLevel(pipelinedSettings, PipelineLevel.SYSTEM_DEFAULTS, path,
                layersSettingsPatches);
        pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);

        if (previousSettingsRepresentation.hasStoredUserSettings()) {
            CS previousUserSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(
                    pipelinedSettings, previousSettingsRepresentation.getUserSettingsRepresentation());
            previousUserSettings = applyPatchesForPipelineLevel(previousUserSettings, PipelineLevel.USER_DEFAULTS, path,
                    layersSettingsPatches);

            pipelinedSettings = SettingsUtil.copyDefaultsFromValues(previousUserSettings, pipelinedSettings);
        } else {
            pipelinedSettings = applyPatchesForPipelineLevel(pipelinedSettings, PipelineLevel.USER_DEFAULTS, path,
                    layersSettingsPatches);
            pipelinedSettings = SettingsUtil.copyDefaultsFromValues(pipelinedSettings, pipelinedSettings);
        }
        pipelinedSettings = SettingsUtil.copyValues(newSettings, pipelinedSettings);
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
    
    /**
     * Internal helper class for management of provided settings patches in relation to its pipeline levels and
     * components that provided patches belong to.
     * 
     * @author Vladislav Chumak
     *
     */
    private static class SettingsPatches {

        private Map<PipelineLevel, Map<List<String>, List<SettingsPatch<? extends Settings>>>> patchesForSettings = new HashMap<>();

        public void addSettingsPatch(List<String> path, SettingsPatch<? extends Settings> settingsPatch,
                PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings
                    .get(pipelineLevel);
            if (pipelinePatches == null) {
                pipelinePatches = new HashMap<>();
                patchesForSettings.put(pipelineLevel, pipelinePatches);
            }
            List<SettingsPatch<? extends Settings>> componentPatches = pipelinePatches.get(path);
            if (componentPatches == null) {
                componentPatches = new ArrayList<>();
                pipelinePatches.put(path, componentPatches);
            }
            componentPatches.add(settingsPatch);
        }

        public Map<List<String>, List<SettingsPatch<? extends Settings>>> getSettingsPatches(
                PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings
                    .get(pipelineLevel);
            if (pipelinePatches == null) {
                return Collections.emptyMap();
            }
            return pipelinePatches;
        }
    }

}