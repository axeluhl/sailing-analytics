package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.common.settings.util.SettingsMergeUtils;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PipelineLevel;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorageAndPatching;
import com.sap.sse.security.ui.settings.SettingsPatch;
import com.sap.sse.security.ui.settings.UserSettingsStorageManagerWithPatching;

public class RaceBoardComponentContext extends ComponentContextWithSettingsStorageAndPatching<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> {

    public RaceBoardComponentContext(
            RaceBoardPerspectiveLifecycle rootLifecycle,
            UserSettingsStorageManagerWithPatching<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> settingsStorageManager) {
        super(rootLifecycle, settingsStorageManager);
    }
    
    public<CS extends GenericSerializableSettings> void addModesPatching(Component<CS> component, CS additiveSettings, OnSettingsPatchedCallback<CS> patchCallback) {
        addPatchForLoadingSettings(component, PipelineLevel.GLOBAL_DEFAULTS, new ModeLoadingSettingsPatch<>(additiveSettings));
        addPatchForStoringSettings(component, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS, new ModeStoringSettingsPatch<>(additiveSettings));
        getInitialSettingsForComponent(component, new OnSettingsLoadedCallback<CS>() {

            @Override
            public void onError(Throwable caught, CS fallbackDefaultSettings) {
                patchCallback.settingsPatched(fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(CS settings) {
                patchCallback.settingsPatched(settings);
            }
        });
    }
    
    public interface OnSettingsPatchedCallback<CS extends Settings> {
        void settingsPatched(CS patchedSettings);
    }
    
    private static class ModeLoadingSettingsPatch<CS extends GenericSerializableSettings> implements SettingsPatch<CS> {
        
        private final CS additiveSettings;

        public ModeLoadingSettingsPatch(CS additiveSettings) {
            this.additiveSettings = additiveSettings;
        }

        @Override
        public CS patchSettings(CS settingsToPatch) {
            SettingsMergeUtils.mergeSettings(additiveSettings, settingsToPatch);
            return settingsToPatch;
        }
        
    }
    
    private static class ModeStoringSettingsPatch<CS extends GenericSerializableSettings> implements SettingsPatch<CS> {
        
        private final CS contextSpecificDefaults;

        public ModeStoringSettingsPatch(CS contextSpecificDefaults) {
            this.contextSpecificDefaults = contextSpecificDefaults;
        }

        @Override
        public CS patchSettings(CS settingsToPatch) {
            SettingsDefaultValuesUtils.setDefaults(contextSpecificDefaults, settingsToPatch);
            return settingsToPatch;
        }
        
    }

}
