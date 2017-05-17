package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
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
public class ComponentContextWithSettingsStorageAndAdditionalSettingsLayers<S extends Settings>
        extends ComponentContextWithSettingsStorage<S> {

    /**
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param userService
     *            The service which is used for server-side settings storage
     * @param storageDefinition
     *            The definition for User Settings and Document Settings storage keys
     */
    public ComponentContextWithSettingsStorageAndAdditionalSettingsLayers(ComponentLifecycle<S> rootLifecycle, UserService userService,
            StoredSettingsLocation storageDefinition) {
        this(rootLifecycle, userService, storageDefinition, new SettingsRepresentationTransformer());
    }

    protected ComponentContextWithSettingsStorageAndAdditionalSettingsLayers(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocation storageDefinition,
            SettingsRepresentationTransformer settingsRepresentationTransformer) {
        this(rootLifecycle, userService, storageDefinition, settingsRepresentationTransformer,
                new UserSettingsBuildingPipelineWithAdditionalSettingsLayers(settingsRepresentationTransformer));
    }

    protected ComponentContextWithSettingsStorageAndAdditionalSettingsLayers(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocation storageDefinition,
            SettingsRepresentationTransformer settingsRepresentationTransformer,
            UserSettingsBuildingPipelineWithPatching settingsBuildingPipeline) {
        super(rootLifecycle, userService, storageDefinition, settingsRepresentationTransformer, settingsBuildingPipeline);
    }

    public <CS extends GenericSerializableSettings> void addAdditionalSettingsLayerForComponent(Component<CS> component, PipelineLevel pipelineLevel,
            CS additionalSettingsLayerForComponent) {
        ((UserSettingsBuildingPipelineWithAdditionalSettingsLayers) settingsBuildingPipeline).addAdditionalSettingsLayer(component,
                pipelineLevel, additionalSettingsLayerForComponent);
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
     *  a custom diff (see {@link GenericSerializableSettings Settings Framework} of the stored settings representation.
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
     * @param reloadedCallback The callback which gets called when the new settings has been reloaded and patched with all patches including the new patch
     */
    public<CS extends GenericSerializableSettings> void addAdditionalSettingsLayerForComponent(Component<CS> component, PipelineLevel pipelineLevel,
            CS additionalSettingsLayerForComponent, OnSettingsReloadedCallback<CS> reloadedCallback) {
        addAdditionalSettingsLayerForComponent(component, pipelineLevel, additionalSettingsLayerForComponent);
        getInitialSettingsForComponent(component, new OnSettingsLoadedCallback<CS>() {

            @Override
            public void onError(Throwable caught, CS fallbackDefaultSettings) {
                onSuccess(fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(CS settings) {
                SettingsDefaultValuesUtils.keepDefaults(component.getSettings(), settings);
                reloadedCallback.onSettingsReloaded(settings);
            }
        });
    }
   /**
    **
    * The callback interface used to accept the reloaded and patched settings.
    * 
    * @author Vladislav Chumak
    *
    * @param <CS> The type of the patched settings
    */
   public interface OnSettingsReloadedCallback<CS extends Settings> {
       
       /**
        * Gets called, when the new patched settings are available.
        * 
        * @param patchedSettings The new patched settings
        */
       void onSettingsReloaded(CS patchedSettings);
   }

}
