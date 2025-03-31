package com.sap.sse.security.ui.settings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.client.shared.settings.ComponentUtils;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;

/**
 * Manages all default settings of perspectives and components. This simple implementation has no support for settings
 * storage. It is only capable of creating new default settings by means of
 * {@link ComponentLifecycle#createDefaultSettings()} of the root component managed by this {@link ComponentContext},
 * considering the URL parameters. If you need settings to be stored, consider
 * {@link ComponentContextWithSettingsStorage}.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class SimpleComponentContext<S extends Settings> implements ComponentContext<S> {

    protected final SettingsRepresentationTransformer settingsRepresentationTransformer;

    /**
     * The pipeline used for the settings construction.
     */
    protected final SettingsBuildingPipeline settingsBuildingPipeline;

    /**
     * The {@link ComponentLifecycle} of the root component/perspective
     */
    protected final ComponentLifecycle<S> rootLifecycle;

    /**
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     */
    public SimpleComponentContext(ComponentLifecycle<S> rootLifecycle) {
        this(rootLifecycle, new SettingsRepresentationTransformer());
    }

    protected SimpleComponentContext(ComponentLifecycle<S> rootLifecycle,
            SettingsRepresentationTransformer settingsRepresentationTransformer) {
        this(rootLifecycle, settingsRepresentationTransformer,
                new UrlSettingsBuildingPipeline(settingsRepresentationTransformer));
    }

    protected SimpleComponentContext(ComponentLifecycle<S> rootLifecycle,
            SettingsRepresentationTransformer settingsRepresentationTransformer,
            SettingsBuildingPipeline settingsBuildingPipeline) {
        this.rootLifecycle = rootLifecycle;
        this.settingsRepresentationTransformer = settingsRepresentationTransformer;
        this.settingsBuildingPipeline = settingsBuildingPipeline;
    }

    /**
     * This operation is unsupported for this simple implementation and will throw a
     * {@link UnsupportedOperationException} when it is called.
     */
    @Override
    public <CS extends Settings> void makeSettingsDefault(Component<CS> component, CS newDefaultSettings,
            final OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newDefaultSettings);
    }

    @Override
    public <CS extends Settings> void resetSettingsToDefault(Component<CS> component, CS newSettings,
            OnSettingsLoadedCallback<CS> onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Settings storage is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newSettings);
    }
    
    /**
     * This operation is unsupported for this simple implementation and will throw a
     * {@link UnsupportedOperationException} when it is called.
     */
    @Override
    public <CS extends Settings> void storeSettingsForContext(Component<CS> component, CS newSettings,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Settings storage is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentLifecycle<S> getRootLifecycle() {
        return rootLifecycle;
    }

    /**
     * Gets the "System Default" {@link Settings} of the root component managed by this {@link ComponentContext}. The
     * returned {@link Settings} should contain all settings for the root component and its subcomponents.
     * 
     * @return The {@link Settings} of the root component
     */
    private S getDefaultSettings() {
        S defaultSettings = rootLifecycle.createDefaultSettings();
        return settingsBuildingPipeline.getSettingsObject(defaultSettings,
                new StorableRepresentationOfDocumentAndUserSettings(null, null));
    }

    /**
     * This method returns always {@code false}, because it does not offer functionality for settings storage.
     */
    @Override
    public boolean isStorageSupported(Component<?> component) {
        return false;
    }

    /**
     * Retrieves the System Default {@link Settings} of the root component managed by this {@link ComponentContext} and
     * passes them to the provided callback.
     * 
     * @see #getDefaultSettings()
     */
    @Override
    public void getInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        onInitialSettingsLoaded.onSuccess(getDefaultSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    }

    /**
     * Retrieves the System Default {@link Settings} of the provided component managed by this {@link ComponentContext}
     * and passes them to the provided callback.
     * 
     * @param component
     *            The component of which the settings should be retrieved
     * @param settingsReceiverCallback
     *            The callback which supplies the caller with initial settings
     */
    @Override
    public <CS extends Settings> void getInitialSettingsForComponent(final Component<CS> component,
            final OnSettingsLoadedCallback<CS> callback) {
        OnSettingsLoadedCallback<S> internalCallback = new OnSettingsLoadedCallback<S>() {

            @Override
            public void onError(Throwable caught, S fallbackDefaultSettings) {
                CS componentFallbackSettings = ComponentUtils.determineComponentSettingsFromPerspectiveSettings(
                        component.getPath(), fallbackDefaultSettings);
                callback.onError(caught, componentFallbackSettings);
            }

            @Override
            public void onSuccess(S settings) {
                CS componentSettings = ComponentUtils.determineComponentSettingsFromPerspectiveSettings(
                        component.getPath(), settings);
                callback.onSuccess(componentSettings);
            }
        };
        getInitialSettings(internalCallback);
    }

}
