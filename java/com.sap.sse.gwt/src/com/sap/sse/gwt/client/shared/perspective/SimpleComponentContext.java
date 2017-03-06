package com.sap.sse.gwt.client.shared.perspective;

import java.util.LinkedList;
import java.util.Queue;

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
     * Contains {@link SettingsReceiverCallback}s which are waiting to receive the initial settings of the root
     * component.
     */
    private Queue<OnSettingsLoadedCallback<S>> settingsReceiverCallbacks = new LinkedList<>();

    /**
     * Current initial/default settings for the whole settings tree which correspond to the root component and its
     * subcomponents.
     */
    private S currentDefaultSettings = null;
    
    /**
     * Indicates whether this context instance is currently loading the initial settings.
     */
    private boolean loadingDefaultSettings = false;

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
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings, final OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Make Default action is unsupported for this type of ComponentContext "
                + this.getClass().getName() + " " + component.getPath() + " " + newDefaultSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentLifecycle<S> getRootLifecycle() {
        return rootLifecycle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public void initInitialSettings() {
        loadDefaultSettingsIfNecessary();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        settingsReceiverCallbacks.add(onInitialSettingsLoaded);
        loadDefaultSettingsIfNecessary();
    }
    
    private void loadDefaultSettingsIfNecessary() {
        if (loadingDefaultSettings) {
            return;
        }
        if (currentDefaultSettings == null) {
            loadingDefaultSettings = true;
            loadDefaultSettings(new OnSettingsLoadedCallback<S>() {
                @Override
                public void onError(Throwable caught, S fallbackDefaultSettings) {
                    loadingDefaultSettings = false;
                    S fallbackSettings = getDefaultSettings();
                    OnSettingsLoadedCallback<S> callback;
                    while ((callback = settingsReceiverCallbacks.poll()) != null) {
                        callback.onError(caught, fallbackSettings);
                    }
                }

                @Override
                public void onSuccess(S result) {
                    loadingDefaultSettings = false;
                    currentDefaultSettings = result;
                    OnSettingsLoadedCallback<S> callback;
                    while ((callback = settingsReceiverCallbacks.poll()) != null) {
                        callback.onSuccess(result);
                    }
                }

            });
        } else {
            OnSettingsLoadedCallback<S> callback;
            while ((callback = settingsReceiverCallbacks.poll()) != null) {
                callback.onSuccess(currentDefaultSettings);
            }
        }
    }
    
    protected void loadDefaultSettings(OnSettingsLoadedCallback<S> callback) {
        callback.onSuccess(getDefaultSettings());
    }

    @Override
    public void dispose() {
    }
}
