package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

/**
 * Manages all default settings of a component/perspective and its subcomponents if there are any. It supplies
 * components with initial settings and determines whether the settings of components are storable or not. This abstract
 * implementation provides settings storage support if the storable settings either implement {@link SettingsMap} or
 * {@link GenericSerializableSettings}. There are two kinds of settings which are stored separately - <b>global
 * settings</b> and <b>context specific settings</b>.
 * <ul>
 * <li>Global settings are the settings which are applied globally to all components</li>
 * <li>Context specific settings have higher precedence than global settings and are applied to components only if the
 * current context (e.g. event or race) matches the context when the settings have been stored.</li>
 * </ul>
 * That means that context specific settings are stored per context (e.g. race or event) whereas global settings are
 * stored globally for all possible contexts (independent of event or race).
 * 
 * 
 * @author Vladislav Chumak
 *
 * @param <L>
 *            The {@link ComponentLifecycle} type of the root component/perspective containing all the settings for
 *            itself and its subcomponents
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public abstract class AbstractComponentContextWithSettingsStorage<L extends ComponentLifecycle<S, ?>, S extends Settings>
        extends SimpleComponentContext<L, S> {

    /**
     * Manages the persistence layer of settings.
     */
    private final SettingsStorageManager<S> settingsStorageManager;

    /**
     * Contains {@link SettingsReceiverCallback}s which are waiting to receive the initial settings of the root
     * component.
     */
    private Queue<SettingsReceiverCallback<S>> settingsReceiverCallbacks = new LinkedList<>();

    /**
     * Current initial/default settings for the whole settings tree which corresponds to the root component and its
     * subcomponents.
     */
    private S currentDefaultSettings = null;

    /**
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param settingsStorageManager
     *            The {@link SettingsStorageManager} to be used access stored settings and store new settings
     */
    public AbstractComponentContextWithSettingsStorage(L rootLifecycle,
            SettingsStorageManager<S> settingsStorageManager) {
        super(rootLifecycle);
        this.settingsStorageManager = settingsStorageManager;
    }

    /**
     * Initialises the instance with initial settings. This method may be called only once during the whole lifecycle of
     * this instance. The call of this method is mandatory, otherwise it will not be possible to obtain initial
     * settings.
     * 
     * @see #initInitialSettings(OnSettingsLoadedCallback)
     */
    public void initInitialSettings() {
        initInitialSettings(null);
    }

    /**
     * Initialises the instance with initial settings. This method may be called only once during the whole lifecycle of
     * this instance. The call of this method is mandatory, otherwise it will not be possible to obtain initial
     * settings.
     * 
     * @param onInitialSettingsLoaded
     *            Callback to be called when the settings initialisation finishes
     * @see #initInitialSettings()
     */
    public void initInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        if (currentDefaultSettings != null) {
            throw new IllegalStateException(
                    "Settings have been already initialized. You may only call this method once.");
        }
        S systemDefaultSettings = rootLifecycle.createDefaultSettings();
        settingsStorageManager.retrieveDefaultSettings(systemDefaultSettings, new OnSettingsLoadedCallback<S>() {

            @Override
            public void onError(Throwable caught, S fallbackDefaultSettings) {
                if (onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onError(caught, fallbackDefaultSettings);
                }
            }

            @Override
            public void onSuccess(S result) {
                currentDefaultSettings = result;
                if (onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onSuccess(rootLifecycle.cloneSettings(result));
                }
                SettingsReceiverCallback<S> callback;
                while ((callback = settingsReceiverCallbacks.poll()) != null) {
                    callback.receiveSettings(rootLifecycle.cloneSettings(result));
                }
            }
        });
    }

    /**
     * Retrieve settings for the root component managed by this context. The provided callback gets called when
     * initial/default settings are available. That means if the initialisation of this instance is finished, the
     * provided callback is called immediately. If initialisation is not done yet, then the callback gets called when
     * the initialisation gets finished and thus, initial settings are available. Make sure to call
     * {@link #initInitialSettings()} when using this method, otherwise the provided callback will be never called.
     * 
     * @param settingsReceiverCallback
     *            The callback which supplies the caller with initial settings
     */
    public void receiveInitialSettings(SettingsReceiverCallback<S> settingsReceiverCallback) {
        if (currentDefaultSettings == null) {
            settingsReceiverCallbacks.add(settingsReceiverCallback);
        } else {
            settingsReceiverCallback.receiveSettings(currentDefaultSettings);
        }
    }

    /**
     * Gets the last error occurred during settings initialisation.
     * 
     * @return The last error as {@link Throwable}, if an error occurred, otherwise {@code null}
     */
    public Throwable getLastError() {
        return settingsStorageManager.getLastError();
    }

    /**
     * Gets the current default {@link Settings} of the root component managed by this {@link ComponentContext}. The
     * returned {@link Settings} should contain all settings for the root component and its subcomponents.
     * 
     * @return The {@link Settings} of the root component
     * @throws IllegalStateException
     *             When the instance has not been initialised yet
     * @see #initInitialSettings()
     */
    @Override
    public S getDefaultSettings() {
        if (currentDefaultSettings == null) {
            throw new IllegalStateException("Settings have not been initialized yet.");
        }
        return rootLifecycle.cloneSettings(currentDefaultSettings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        if (!component.hasSettings()) {
            return false;
        }
        Settings settings = component.getSettings();
        if (settings instanceof SettingsMap || settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public S extractGlobalSettings(Component<? extends Settings> component, Settings newDefaultSettings) {
        L targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        return extractGlobalSettings((S) newDefaultSettings, targetLifeCycle);
    }

    @SuppressWarnings("unchecked")
    public S extractContextSettings(Component<? extends Settings> component, Settings newDefaultSettings) {
        L targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        return extractContextSettings((S) newDefaultSettings, targetLifeCycle);
    }

    private S extractGlobalSettings(S newDefaultSettings, L targetLifeCycle) {
        return targetLifeCycle.extractGlobalSettings((S) newDefaultSettings);
    }

    private S extractContextSettings(S newDefaultSettings, L targetLifeCycle) {
        return targetLifeCycle.extractContextSettings((S) newDefaultSettings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        L targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        S globalSettings = extractGlobalSettings((S) newDefaultSettings, targetLifeCycle);
        S contextSettings = extractContextSettings((S) newDefaultSettings, targetLifeCycle);
        updateSettings(component.getPath(), globalSettings, contextSettings);
    }

    private JSONObject patchJsonObject(JSONObject root, List<String> path, Settings newSettings) {
        String current = path.remove(path.size() - 1);
        // we need to go further
        if (!path.isEmpty()) {
            JSONValue child = root.get(current);
            boolean haskey = root.containsKey(current);
            if (child == null) {
                if (haskey) {
                    GWT.log("Warning: replacing some subtree element that is wrong type!");
                }
                child = new JSONObject();
            }
            root.put(current, child);
            return patchJsonObject(root, path, newSettings);
        } else {
            if (root == null) {
                root = new JSONObject();
            }
            JSONValue json = settingsStorageManager.settingsToJSON(newSettings);
            root.put(current, json);
        }
        return root;
    }

    private void updateSettings(final ArrayList<String> path, final Settings globalSettings,
            final Settings contextSettings) {
        Window.alert("Save for " + path + "  Global " + globalSettings + " Context " + contextSettings);
        settingsStorageManager.retrieveGlobalSettingsJson(new AsyncCallback<JSONObject>() {
            @Override
            public void onSuccess(final JSONObject globalServerside) {
                settingsStorageManager.retrieveContextSpecificSettingsJson(new AsyncCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject contextServerside) {
                        JSONObject patchedContext = patchJsonObject(contextServerside, new ArrayList<>(path),
                                contextSettings);
                        JSONObject patchedGlobal = patchJsonObject(globalServerside, new ArrayList<>(path),
                                globalSettings);

                        settingsStorageManager.storeGlobalSettings(patchedGlobal);
                        settingsStorageManager.storeContextSpecificSettings(patchedContext);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Could not delta data, " + caught);
                    }
                });
            }
            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Could not delta data, " + caught);
            }
        });
    }

    /**
     * Travels the Component tree, to find the correct LifeCycle for the Rootcomponent of the subtree to save
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private L determineLifeCycle(ArrayList<String> path, L current) {
        while (current instanceof PerspectiveLifecycle<?> && !path.isEmpty()) {
            String last = path.remove(path.size() - 1);
            current = (L) ((PerspectiveLifecycle) current).getLiveCycleForId(last);
        }
        if (!path.isEmpty()) {
            throw new IllegalStateException("Settings path is not finished, but no perspective at current level");
        }
        return current;
    }
}
