package com.sap.sse.security.ui.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsSerializationHelper;

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
public class UserSettingsBuildingPipelineWithPatching extends UserSettingsBuildingPipeline {

    private SettingsPatches patchesForStoringSettings = new SettingsPatches();
    private SettingsPatches patchesForLoadingSettings = new SettingsPatches();

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its JSON representation.
     * 
     * @param settingsSerializationHelper
     *            The custom conversion helper
     */
    public UserSettingsBuildingPipelineWithPatching(SettingsSerializationHelper settingsStringConverter) {
        super(settingsStringConverter);
    }

    /**
     * Constructs a settings object by means of provided defaultSettings, persisted representations of User Settings and
     * Document Settings, current URL, and loading patches, which have been added to this pipeline instance.
     * 
     * @param defaultSettings
     *            The basic settings to be used
     * @param settingsJsons
     *            The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    @Override
    public <CS extends Settings> CS getSettingsObject(CS defaultSettings, SettingsJsons settingsJsons) {
        boolean ignoreLocalSettings = isIgnoreLocalSettingsUrlFlagPresent();
        
        defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.SYSTEM_DEFAULTS);
        if (!ignoreLocalSettings && settingsJsons.getContextSpecificSettingsJson() != null) {
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.GLOBAL_DEFAULTS);
            defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                    settingsJsons.getContextSpecificSettingsJson());
        } else if (!ignoreLocalSettings && settingsJsons.getGlobalSettingsJson() != null) {
            defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                    settingsJsons.getGlobalSettingsJson());
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.GLOBAL_DEFAULTS);
        } else {
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.GLOBAL_DEFAULTS);
        }
        defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS);
        defaultSettings = settingsSerializationHelper.deserializeFromCurrentUrl(defaultSettings);
        return defaultSettings;
    }

    private <CS extends Settings> CS applyPatchesForPipelineLevel(CS defaultSettings, PipelineLevel pipelineLevel) {
        for (Entry<List<String>, List<SettingsPatch<? extends Settings>>> entry : patchesForLoadingSettings
                .getSettingsPatches(pipelineLevel).entrySet()) {
            List<String> path = entry.getKey();
            List<SettingsPatch<? extends Settings>> settingsPatches = entry.getValue();
            if (!settingsPatches.isEmpty()) {
                Settings patchedComponentSettings = ComponentUtils
                        .determineComponentSettingsFromPerspectiveSettings(new ArrayList<>(path), defaultSettings);
                for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
                    patchedComponentSettings = patchSettings(patchedComponentSettings, settingsPatch);
                }
                defaultSettings = ComponentUtils.patchSettingsTree(new ArrayList<>(path), patchedComponentSettings,
                        defaultSettings);
            }
        }
        return defaultSettings;
    }

    /**
     * Converts the provided settings object into a JSON representation considering storing patches, which have been
     * added to this pipeline instance.
     * 
     * @param settings
     *            The settings to convert to JSON representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The path of the settings in the settings tree
     * @return The JSON representation of the provided settings
     */
    @Override
    public JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path) {
        for (PipelineLevel level : pipelineLevel.getSortedLevelsUntilCurrent()) {
            List<SettingsPatch<? extends Settings>> settingsPatches = patchesForStoringSettings.getSettingsPatches(path,
                    level);
            for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
                settings = patchSettings(settings, settingsPatch);
            }
        }
        return super.getJsonObject(settings, pipelineLevel, path);
    }

    @SuppressWarnings("unchecked")
    private <CS extends Settings> CS patchSettings(Settings settings, SettingsPatch<CS> settingsPatch) {
        return settingsPatch.patchSettings((CS) settings);
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
        patchesForStoringSettings.addSettingsPatch(component.getPath(), settingsPatch, pipelineLevel);
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
        patchesForLoadingSettings.addSettingsPatch(component.getPath(), settingsPatch, pipelineLevel);
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

        public List<SettingsPatch<? extends Settings>> getSettingsPatches(List<String> path,
                PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings
                    .get(pipelineLevel);
            if (pipelinePatches == null) {
                return Collections.emptyList();
            }
            List<SettingsPatch<? extends Settings>> componentPatches = pipelinePatches.get(path);
            if (componentPatches == null) {
                return Collections.emptyList();
            }
            return componentPatches;
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