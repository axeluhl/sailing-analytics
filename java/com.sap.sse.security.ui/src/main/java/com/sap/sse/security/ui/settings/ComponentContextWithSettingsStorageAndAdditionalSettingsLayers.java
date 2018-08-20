package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.security.ui.client.UserService;

/**
 * Specialization of {@link ComponentContextWithSettingsStorageAndAdditionalSettingsLayers} which offers multiple hooks
 * for adding of additional settings layers.
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
    public ComponentContextWithSettingsStorageAndAdditionalSettingsLayers(ComponentLifecycle<S> rootLifecycle,
            UserService userService, StoredSettingsLocation storageDefinition) {
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
            UserSettingsBuildingPipelineWithAdditionalSettingsLayers settingsBuildingPipeline) {
        super(rootLifecycle, userService, storageDefinition, settingsRepresentationTransformer,
                settingsBuildingPipeline);
    }

    /**
     * Adds an additional settings layer with provided layer settings to the corresponding component. A component may
     * have multiple layer settings. The effective settings are patched by the additional settings layer in the
     * following way: All settings values of provided {@code additionalLayerSettings} parameter, which are set to a
     * non-default value will override the resulting settings values. When the settings value type is a collection, the
     * values of {@code additionalLayerSettings} and the resulting settings will be merged as following (read
     * carefully):
     * <ul>
     * <li>If {@code additionalLayerSettings} <b>default values</b> contain a <i>value</i>, which is not contained in
     * the <b>values</b> of {@code additiveSettings}, the not contained <i>value</i> gets removed from the <i>resulting
     * settings values</i></li>
     * <li>If {@code additionalLayerSettings} <b>values</b> contain a <i>value</i>, which is not contained in the
     * <b>values</b> of the resulting settings, the not contained value gets added to the <i>resulting settings
     * values</i>
     * </ul>
     * The additional settings layer shows its effect by further calls of:
     * <ul>
     * <li>{@link #getInitialSettings(OnSettingsLoadedCallback)}</li>
     * <li>{@link #getInitialSettingsForComponent(Component, OnSettingsLoadedCallback)}</li>
     * <li>{@link #makeSettingsDefault(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * <li>{@link #storeSettingsForContext(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * </ul>
     * 
     * @param component
     *            The targeted component which the provided layer settings belong to
     * @param afterSettingsLayer
     *            The pipeline level <b>AFTER</b> that the provided layer is going to apply
     * @param additionalLayerSettings
     *            The layer settings to apply on top the effective settings after the settings layer at provided level
     */
    public <CS extends GenericSerializableSettings> void addAdditionalSettingsLayerForComponent(Component<CS> component,
            PipelineLevel pipelineLevel, CS additionalSettingsLayerForComponent) {
        ((UserSettingsBuildingPipelineWithAdditionalSettingsLayers) settingsBuildingPipeline)
                .addAdditionalSettingsLayer(component, pipelineLevel, additionalSettingsLayerForComponent);
    }

    /**
     * Adds an additional settings layer with provided layer settings to the corresponding component. A component may
     * have multiple layer settings. The effective settings are patched by the additional settings layer in the
     * following way: All settings values of provided {@code additionalLayerSettings} parameter, which are set to a
     * non-default value will override the resulting settings values. When the settings value type is a collection, the
     * values of {@code additionalLayerSettings} and the resulting settings will be merged as following (read
     * carefully):
     * <ul>
     * <li>If {@code additionalLayerSettings} <b>default values</b> contain a <i>value</i>, which is not contained in
     * the <b>values</b> of {@code additiveSettings}, the not contained <i>value</i> gets removed from the <i>resulting
     * settings values</i></li>
     * <li>If {@code additionalLayerSettings} <b>values</b> contain a <i>value</i>, which is not contained in the
     * <b>values</b> of the resulting settings, the not contained value gets added to the <i>resulting settings
     * values</i>
     * </ul>
     * The additional settings layer shows its effect by further calls of:
     * <ul>
     * <li>{@link #getInitialSettings(OnSettingsLoadedCallback)}</li>
     * <li>{@link #getInitialSettingsForComponent(Component, OnSettingsLoadedCallback)}</li>
     * <li>{@link #makeSettingsDefault(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * <li>{@link #storeSettingsForContext(Component, Settings, com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback)}</li>
     * </ul>
     * 
     * @param component
     *            The targeted component which the provided layer settings belong to
     * @param afterSettingsLayer
     *            The pipeline level <b>AFTER</b> that the provided layer is going to apply
     * @param additionalLayerSettings
     *            The layer settings to apply on top the effective settings after the settings layer at provided level
     * @param reloadedCallback
     *            The callback used to accept the reloaded settings after adding an additional settings layer
     */
    public <CS extends GenericSerializableSettings> void addAdditionalSettingsLayerForComponent(Component<CS> component,
            PipelineLevel pipelineLevel, CS additionalSettingsLayerForComponent,
            OnSettingsReloadedCallback<CS> reloadedCallback) {
        addAdditionalSettingsLayerForComponent(component, pipelineLevel, additionalSettingsLayerForComponent);
        getInitialSettingsForComponent(component, new OnSettingsLoadedCallback<CS>() {

            @Override
            public void onError(Throwable caught, CS fallbackDefaultSettings) {
                onSuccess(fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(CS settings) {
                reloadedCallback.onSettingsReloaded(settings);
            }
        });
    }

    /**
     **
     * The callback interface used to accept the reloaded settings after adding an additional settings layer.
     * 
     * @author Vladislav Chumak
     *
     * @param <CS>
     *            The type of the reloaded settings
     */
    public interface OnSettingsReloadedCallback<CS extends Settings> {

        /**
         * Gets called, when settings are available after reload considering all previously added additional settings
         * layers.
         * 
         * @param patchedSettings
         *            The reloaded settings
         */
        void onSettingsReloaded(CS patchedSettings);
    }

}
