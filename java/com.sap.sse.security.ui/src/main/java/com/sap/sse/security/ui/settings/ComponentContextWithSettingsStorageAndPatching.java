package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.ComponentContextWithSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.PipelineLevel;

public class ComponentContextWithSettingsStorageAndPatching<S extends Settings> extends ComponentContextWithSettingsStorage<S> {
    
    protected final UserSettingsStorageManagerWithPatching<S> settingsStorageManager;
    
    public ComponentContextWithSettingsStorageAndPatching(ComponentLifecycle<S> rootLifecycle, UserSettingsStorageManagerWithPatching<S> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
        this.settingsStorageManager = (UserSettingsStorageManagerWithPatching<S>) super.settingsStorageManager;
    }
    
    public<CS extends Settings> void addPatchForStoringSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsStorageManager.addPatchForStoringSettings(component, pipelineLevel, settingsPatch);
    }
    
    public<CS extends Settings> void addPatchForLoadingSettings(Component<CS> component, PipelineLevel pipelineLevel, SettingsPatch<CS> settingsPatch) {
        settingsStorageManager.addPatchForLoadingSettings(component, pipelineLevel, settingsPatch);
    }

}
