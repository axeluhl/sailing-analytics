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
import com.sap.sse.gwt.client.shared.perspective.ComponentUtils;
import com.sap.sse.gwt.client.shared.perspective.PipelineLevel;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;
import com.sap.sse.gwt.client.shared.perspective.SettingsStringConverter;

class UserSettingsBuildingPipelineWithPatching extends UserSettingsBuildingPipeline {
    
    private SettingsPatches patchesForStoringSettings = new SettingsPatches();
    private SettingsPatches patchesForLoadingSettings = new SettingsPatches();
    
    public UserSettingsBuildingPipelineWithPatching(SettingsStringConverter settingsStringConverter) {
        super(settingsStringConverter);
    }
    
    public UserSettingsBuildingPipelineWithPatching() {
    }
    
    @Override
    public <CS extends Settings> CS getSettingsObject(CS defaultSettings, SettingsJsons settingsJsons) {
        defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.SYSTEM_DEFAULTS);
        if(settingsJsons.getContextSpecificSettingsJson() != null) {
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.GLOBAL_DEFAULTS);
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getContextSpecificSettingsJson());
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS);
        } else if (settingsJsons.getGlobalSettingsJson() != null) {
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getGlobalSettingsJson());
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.GLOBAL_DEFAULTS);
            defaultSettings = applyPatchesForPipelineLevel(defaultSettings, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS);
        }
        defaultSettings = settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
        return defaultSettings;
    }

    private <CS extends Settings> CS applyPatchesForPipelineLevel(CS defaultSettings, PipelineLevel pipelineLevel) {
        for (Entry<List<String>, List<SettingsPatch<? extends Settings>>> entry : patchesForLoadingSettings.getSettingsPatches(pipelineLevel).entrySet()) {
            List<String> path = entry.getKey();
            List<SettingsPatch<? extends Settings>> settingsPatches = entry.getValue();
            if(!settingsPatches.isEmpty()) {
                Settings patchedComponentSettings = ComponentUtils.determineComponentSettingsFromPerspectiveSettings(new ArrayList<>(path), defaultSettings);
                for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
                    patchedComponentSettings = patchSettings(patchedComponentSettings, settingsPatch);
                }
                defaultSettings = ComponentUtils.patchSettingsTree(new ArrayList<>(path), patchedComponentSettings, defaultSettings);
            }
        }
        return defaultSettings;
    }
    
    @Override
    public JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path) {
        List<SettingsPatch<? extends Settings>> settingsPatches = patchesForStoringSettings.getSettingsPatches(path, pipelineLevel);
        for (SettingsPatch<? extends Settings> settingsPatch : settingsPatches) {
            settings = patchSettings(settings, settingsPatch);
        }
        return super.getJsonObject(settings, pipelineLevel, path);
    }

    @SuppressWarnings("unchecked")
    private<CS extends Settings> CS patchSettings(Settings settings, SettingsPatch<CS> settingsPatch) {
        return settingsPatch.patchSettings((CS) settings);
    }
    
    public<CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        patchesForStoringSettings.addSettingsPatch(component.getPath(), settingsPatch, pipelineLevel);
    }
    
    public<CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        patchesForLoadingSettings.addSettingsPatch(component.getPath(), settingsPatch, pipelineLevel);
    }
    
    private static class SettingsPatches {
        
        private Map<PipelineLevel, Map<List<String>, List<SettingsPatch<? extends Settings>>>> patchesForSettings = new HashMap<>();
        
        public void addSettingsPatch(List<String> path, SettingsPatch<? extends Settings> settingsPatch, PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings.get(pipelineLevel);
            if(pipelinePatches == null) {
                pipelinePatches = new HashMap<>();
                patchesForSettings.put(pipelineLevel, pipelinePatches);
            }
            List<SettingsPatch<? extends Settings>> componentPatches = pipelinePatches.get(path);
            if(componentPatches == null) {
                componentPatches = new ArrayList<>();
                pipelinePatches.put(path, componentPatches);
            }
            componentPatches.add(settingsPatch);
        }
        
        public List<SettingsPatch<? extends Settings>> getSettingsPatches(List<String> path, PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings.get(pipelineLevel);
            if(pipelinePatches == null) {
                return Collections.emptyList();
            }
            List<SettingsPatch<? extends Settings>> componentPatches = pipelinePatches.get(path);
            if(componentPatches == null) {
                return Collections.emptyList();
            }
            return componentPatches;
        }
        
        public Map<List<String>, List<SettingsPatch<? extends Settings>>> getSettingsPatches(PipelineLevel pipelineLevel) {
            Map<List<String>, List<SettingsPatch<? extends Settings>>> pipelinePatches = patchesForSettings.get(pipelineLevel);
            if(pipelinePatches == null) {
                return Collections.emptyMap();
            }
            return pipelinePatches;
        }
    }
    
}