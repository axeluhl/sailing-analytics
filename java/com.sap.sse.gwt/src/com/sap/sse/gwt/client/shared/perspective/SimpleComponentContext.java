package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default settings of perspectives and components. This simple implementation has no support for settings
 * storage. It is only capable of creating new default settings by means of {@link ComponentLifecycle} of the root
 * component managed by this {@link ComponentContext}. All in all, it is a dummy implementation for components which do
 * not have support for settings storage. If you need settings to be stored, consider
 * {@link ComponentContextWithSettingsStorage}.
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class SimpleComponentContext<S extends Settings> implements ComponentContext<S> {

    protected final ComponentLifecycle<S> rootLifecycle;

    /**
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     */
    public SimpleComponentContext(ComponentLifecycle<S> rootLifecycle) {
        this.rootLifecycle = rootLifecycle;
    }

    /**
     * This operation is unsupported for this simple implementation and will throw a
     * {@link UnsupportedOperationException} when it is called.
     */
    @Override
    public<CS extends Settings> void makeSettingsDefault(Component<CS> component, CS newDefaultSettings, final OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newDefaultSettings);
    }
    
    @Override
    public<CS extends Settings> void storeSettingsForContext(Component<CS> component, CS newSettings, OnSettingsStoredCallback onSettingsStoredCallback) {
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
     * Gets the current default {@link Settings} of the root component managed
     * by this {@link ComponentContext}. The returned {@link Settings} should
     * contain all settings for the root component and its subcomponents.
     * 
     * @return The {@link Settings} of the root component
     */
    public S getDefaultSettings() {
        return rootLifecycle.createDefaultSettings();
    }

    /**
     * This method returns always {@code false}, because it does not offer functionality for settings storage.
     */
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void getInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        onInitialSettingsLoaded.onSuccess(getDefaultSettings());
    }
    
    @Override
    public void dispose() {
    }
    
    @Override
    public <CS extends Settings> void getInitialSettingsForComponent(final Component<CS> component, final OnSettingsLoadedCallback<CS> callback) {
        ComponentLifecycle<CS> componentLifecycle = ComponentUtils.determineLifecycle(new ArrayList<>(component.getPath()), rootLifecycle);
        callback.onSuccess(componentLifecycle.createDefaultSettings());
    }

}
