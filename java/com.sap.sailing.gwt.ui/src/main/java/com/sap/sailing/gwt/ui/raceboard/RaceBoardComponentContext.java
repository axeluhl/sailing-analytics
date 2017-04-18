package com.sap.sailing.gwt.ui.raceboard;

import java.util.Map;

import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.Setting;
import com.sap.sse.common.settings.generic.ValueCollectionSetting;
import com.sap.sse.common.settings.generic.ValueSetting;
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
        addPatchForLoadingSettings(component, PipelineLevel.GLOBAL_DEFAULTS, new ModeStoringSettingsPatch<>(additiveSettings));
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
            applyPatchToSetting(additiveSettings, settingsToPatch);
            return settingsToPatch;
        }
        
        //TODO think about extracting of the following additive/merging functionality for settings to an external class
        private static void applyPatchToSetting(GenericSerializableSettings additiveSettings, GenericSerializableSettings settingsToPatch) {
            for (Map.Entry<String, Setting> entry : additiveSettings.getChildSettings().entrySet()) {
                Setting setting = entry.getValue();
                Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
                if (!setting.isDefaultValue()) {
                    if (setting instanceof ValueSetting) {
                        applyPatchToSetting((ValueSetting<?>) setting, (ValueSetting<?>) settingToPatch);
                    } else if (setting instanceof ValueCollectionSetting) {
                        applyPatchToSetting((ValueCollectionSetting<?>) setting, (ValueCollectionSetting<?>) settingToPatch);
                    } else if (setting instanceof GenericSerializableSettings) {
                        applyPatchToSetting((GenericSerializableSettings) setting, (GenericSerializableSettings) settingToPatch);
                    } else {
                        throw new IllegalStateException("Unknown Setting type");
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static<T> void applyPatchToSetting(ValueCollectionSetting<?> setting, ValueCollectionSetting<T> settingToPatch) {
            for (Object value : setting.getValues()) {
                if(!Util.contains(settingToPatch.getValues(), value)) {
                    settingToPatch.addValue((T) value);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static<T> void applyPatchToSetting(ValueSetting<?> setting, ValueSetting<T> settingToPatch) {
            settingToPatch.setValue((T) setting.getValue());
        }
        
    }
    
    private static class ModeStoringSettingsPatch<CS extends GenericSerializableSettings> implements SettingsPatch<CS> {
        
        private final CS contextSpecificDefaults;

        public ModeStoringSettingsPatch(CS contextSpecificDefaults) {
            this.contextSpecificDefaults = contextSpecificDefaults;
        }

        @Override
        public CS patchSettings(CS settingsToPatch) {
            applyDefaults(contextSpecificDefaults, settingsToPatch);
            return null;
        }
        
        //TODO think about extracting of the following default value adapting functionality for settings to an external class
        private static void applyDefaults(GenericSerializableSettings defaultSettings, GenericSerializableSettings settingsToPatch) {
            for (Map.Entry<String, Setting> entry : defaultSettings.getChildSettings().entrySet()) {
                Setting defaultSetting = entry.getValue();
                Setting settingToPatch = settingsToPatch.getChildSettings().get(entry.getKey());
                if (defaultSetting instanceof ValueSetting) {
                    applyDefaults((ValueSetting<?>) defaultSetting, (ValueSetting<?>) settingToPatch);
                } else if (defaultSetting instanceof ValueCollectionSetting) {
                    applyDefaults((ValueCollectionSetting<?>) defaultSetting, (ValueCollectionSetting<?>) settingToPatch);
                } else if (defaultSetting instanceof GenericSerializableSettings) {
                    applyDefaults((GenericSerializableSettings) defaultSetting, (GenericSerializableSettings) settingToPatch);
                } else {
                    throw new IllegalStateException("Unknown Setting type");
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static<T> void applyDefaults(ValueCollectionSetting<?> defaultSetting, ValueCollectionSetting<T> settingToPatch) {
            Iterable<T> originalValues = settingToPatch.getValues();
            settingToPatch.setDefaultValues((Iterable<T>) defaultSetting.getValues());
            settingToPatch.setValues(originalValues);
        }

        @SuppressWarnings("unchecked")
        private static<T> void applyDefaults(ValueSetting<?> defaultSetting, ValueSetting<T> settingToPatch) {
            T originalValue = settingToPatch.getValue();
            settingToPatch.setDefaultValue((T) defaultSetting.getValue());
            settingToPatch.setValue(originalValue);
        }
        
    }

}
