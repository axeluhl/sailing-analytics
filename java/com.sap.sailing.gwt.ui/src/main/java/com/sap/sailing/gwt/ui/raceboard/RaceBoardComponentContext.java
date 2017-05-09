package com.sap.sailing.gwt.ui.raceboard;

import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.common.settings.util.SettingsMergeUtils;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorage;
import com.sap.sse.security.ui.settings.ComponentContextWithSettingsStorageAndPatching;
import com.sap.sse.security.ui.settings.SettingsPatch;
import com.sap.sse.security.ui.settings.StorageDefinition;

/**
 * A specialization of {@link ComponentContextWithSettingsStorageAndPatching} which is specially designed for
 * handling of RaceModes dependent default settings. This implementation offers convenience methods that can
 * be used by RaceModes to add Mode-specific default settings and inject them in the underlying settings building
 * pipeline.
 * 
 * 
 * @author Vladislav Chumak
 * @see ComponentContextWithSettingsStorageAndPatching
 * @see ComponentContextWithSettingsStorage
 *
 */
public class RaceBoardComponentContext extends ComponentContextWithSettingsStorageAndPatching<PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings>> {

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public RaceBoardComponentContext(
            RaceBoardPerspectiveLifecycle rootLifecycle,
            UserService userService, StorageDefinition storageDefinition) {
        super(rootLifecycle, userService, storageDefinition);
    }
    
    /**
     * Adds new settings patches in order to customize default settings construction according to the current race mode.
     * There are two settings patches which are constructed and added to the underlying settings building pipeline - 
     * a patch for settings object construction (loading settings), and a patch for settings storing.
     * 
     * <dl>
     *  <dt>Loading settings patch</dt>
     *  <dd>A loading settings patch is applied on top of the User Settings, when the settings object is constructed. 
     *  The settings are patched in the following way:
     *  All settings values of provided {@code additiveSettings} parameter, which are set to a non-default value
     *  will override the resulting settings values. When the settings value type is a collection, the values of
     *  {@code additiveSettings} and the resulting settings will be merged as following (read carefully):
     *  <ul>
     *   <li>If {@code additiveSettings} <b>default values</b> contain a <i>value</i>, which is not contained in the <b>values</b> of
     *   {@code additiveSettings}, the not contained <i>value</i> gets removed from the <i>resulting settings values</i></li>
     *   <li>If {@code additiveSettings} <b>values</b> contain a <i>value</i>, which is not contained in the <b>values</b> of
     *   the resulting settings, the not contained value gets added to the <i>resulting settings values</i>
     *  </ul>
     *  </dd>
     *  
     *  <dt>Storing settings patch</dt>
     *  <dd>A storing settings patch is applied on top of the Document Settings, when the settings are stored for a context (see
     *  {@link #storeSettingsForContext(Component, Settings, com.sap.sse.gwt.client.shared.perspective.OnSettingsStoredCallback)})
     *  The patch implementation patches the <b>default values</b> of the resulting settings to be stored in order to produce
     *  a custom diff (see {@link GenericSerializableSettings Settings Framework} of the persisted settings representation.
     *  That means, in RaceBoard the user settings are serialized and stored by diffing with <b>System Default Settings</b>,
     *  and Document Settings are serialized and stored by diffing with <b>Mode-Default Settings</b>. In order to achieve this,
     *  the <b>default values</b> of the resulting settings are patched in the following way:
     *  
     *  All settings values of provided {@code additiveSettings} parameter, which are set to a non-default value
     *  will override the <b>default values</b> of the resulting settings. When the settings value type is a collection, the values of
     *  {@code additiveSettings} and the <b>default values</b> of the resulting settings will be merged as following (read carefully):
     *  <ul>
     *   <li>If {@code additiveSettings} <b>default values</b> contain a <i>value</i>, which is not contained in the <b>values</b> of
     *   {@code additiveSettings}, the not contained <i>value</i> gets removed from the <i>resulting settings default values</i></li>
     *   <li>If {@code additiveSettings} <b>values</b> contain a <i>value</i>, which is not contained in the <b>values</b> of
     *   the resulting settings, the not contained value gets added to the <i>resulting settings default values</i>
     *  </ul>
     *  </dd>
     * 
     * @param component The component which maintains the target settings for the provided settings patch
     * @param additiveSettings The additiveSettings for settings patches (see description above)
     * @param patchCallback The callback which gets called when the new settings has been reloaded and patched with all patches including the new patch
     */
    public<CS extends GenericSerializableSettings> void addModesPatching(Component<CS> component, CS additiveSettings, OnSettingsPatchedCallback<CS> patchCallback) {
        addPatchForLoadingSettings(component, PipelineLevel.GLOBAL_DEFAULTS, new ModeLoadingSettingsPatch<>(additiveSettings));
        addPatchForStoringSettings(component, PipelineLevel.CONTEXT_SPECIFIC_DEFAULTS, new ModeStoringSettingsPatch<>(additiveSettings));
        getInitialSettingsForComponent(component, new OnSettingsLoadedCallback<CS>() {

            @Override
            public void onError(Throwable caught, CS fallbackDefaultSettings) {
                onSuccess(fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(CS settings) {
                SettingsDefaultValuesUtils.keepDefaults(component.getSettings(), settings);
                patchCallback.settingsPatched(settings);
            }
        });
    }
    
    /**
     * The callback interface used to accept the reloaded and patched settings.
     * 
     * @author Vladislav Chumak
     *
     * @param <CS> The type of the patched settings
     */
    public interface OnSettingsPatchedCallback<CS extends Settings> {
        
        /**
         * Gets called, when the new patched settings are available.
         * 
         * @param patchedSettings The new patched settings
         */
        void settingsPatched(CS patchedSettings);
    }
    
    /**
     * Patch for Loading Settings pipeline
     * 
     * @author Vladislav Chumak
     *
     * @param <CS> The type of the settings to patch
     */
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
    
    /**
     * Patch for Storing Settings pipeline
     * 
     * @author Vladislav Chumak
     *
     * @param <CS> The type of the settings to patch
     */
    private static class ModeStoringSettingsPatch<CS extends GenericSerializableSettings> implements SettingsPatch<CS> {
        
        private final CS contextSpecificDefaults;

        public ModeStoringSettingsPatch(CS contextSpecificDefaults) {
            this.contextSpecificDefaults = contextSpecificDefaults;
        }

        @Override
        public CS patchSettings(CS settingsToPatch) {
            SettingsMergeUtils.mergeDefaults(contextSpecificDefaults, settingsToPatch);
            return settingsToPatch;
        }
        
    }

}
