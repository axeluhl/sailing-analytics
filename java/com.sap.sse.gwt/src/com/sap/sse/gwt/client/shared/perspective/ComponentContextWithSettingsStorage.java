package com.sap.sse.gwt.client.shared.perspective;

import java.util.ArrayList;
import java.util.List;

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
 * Manages all the default settings of a component/perspective and its subcomponents if there are any. It supplies
 * components with initial settings and determines whether the settings of components are storable or not. This
 * implementation provides settings storage support if the storable settings either implement {@link SettingsMap} or
 * {@link GenericSerializableSettings}. There are two kinds of settings which are stored separately - <b>global
 * settings</b> and <b>context specific settings</b>.
 * <ul>
 * <li>Global settings are the settings which are applied globally to all components</li>
 * <li>Context specific settings have higher precedence than global settings and are applied to components only if the
 * current context (e.g. event or race) matches the context of stored settings.</li>
 * </ul>
 * That means that context specific settings are stored per context (e.g. race or event) whereas global settings are
 * stored globally for all possible contexts (independent of event or race).
 * 
 * 
 * @author Vladislav Chumak
 *
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 */
public class ComponentContextWithSettingsStorage<S extends Settings> extends SimpleComponentContext<S> {

    /**
     * Manages the persistence layer of settings.
     */
    private final SettingsStorageManager<S> settingsStorageManager;

    /**
     * 
     * @param settingsStorageManager
     *            The {@link SettingsStorageManager} to be used to access stored settings and to store new settings
     */
    public ComponentContextWithSettingsStorage(ComponentLifecycle<S> rootLifecycle,
            SettingsStorageManager<S> settingsStorageManager) {
        super(rootLifecycle);
        this.settingsStorageManager = settingsStorageManager;
    }
    
    @Override
    protected void loadDefaultSettings(OnSettingsLoadedCallback<S> callback) {
        S systemDefaultSettings = rootLifecycle.createDefaultSettings();
        settingsStorageManager.retrieveDefaultSettings(systemDefaultSettings, callback);
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
     * Checks whether the {@link Settings} of the provided {@link Component} are
     * storable and whether the underlying implementation supports
     * {@link #makeSettingsDefault(Component, Settings)} calls for it.
     * 
     * @param component The component with potentially storable settings
     * @return {@code true} if the settings of the component are storable
     * <b>AND</b> the {@link ComponentContext} implementation has settings
     * storage support
     */
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        if (!component.hasSettings()) {
            return false;
        }
        if(!settingsStorageManager.supportsStore()) {
            return false;
        }
        Settings settings = component.getSettings();
        if (settings instanceof SettingsMap || settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }

    /**
     * Extracts global settings from provided {@link Settings} of the specified component.
     * 
     * @param component The settings of the specified component
     * @param componentSettings The settings of the specified component
     * @return The global settings extracted, or {@code null} if there aren't any
     * global settings to be stored
     */
    @SuppressWarnings("unchecked")
    public S extractGlobalSettings(Component<? extends Settings> component, Settings componentSettings) {
        ComponentLifecycle<S> targetLifecycle = determineLifecycle(component.getPath(), rootLifecycle);
        return extractGlobalSettings((S) componentSettings, targetLifecycle);
    }

    /**
     * Extracts context specific settings from provided {@link Settings} of the specified component.
     * 
     * @param component The component which the provided settings correspond to
     * @param componentSettings The settings of the specified component
     * @return The context specific settings extracted, or {@code null} if there aren't any
     * context specific settings to be stored
     */
    @SuppressWarnings("unchecked")
    public S extractContextSettings(Component<? extends Settings> component, Settings componentSettings) {
        ComponentLifecycle<S> targetLifeCycle = determineLifecycle(component.getPath(), rootLifecycle);
        return extractContextSettings((S) componentSettings, targetLifeCycle);
    }

    /**
     * Extracts global settings from provided {@link Settings} of the component corresponding to the specified lifecycle.
     * 
     * @param componentSettings The settings of the specified component
     * @param targetLifecycle The lifecycle of the component which the provided settings correspond to
     * @return The global settings extracted, or {@code null} if there aren't any
     * global settings to be stored
     */
    private S extractGlobalSettings(S componentSettings, ComponentLifecycle<S> targetLifecycle) {
        return targetLifecycle.extractGlobalSettings((S) componentSettings);
    }

    /**
     * Extracts context specific settings from provided {@link Settings} of the component corresponding to the specified lifecycle.
     * 
     * @param componentSettings The settings of the specified component
     * @param targetLifecycle The lifecycle of the component which the provided settings correspond to
     * @return The context specific settings extracted, or {@code null} if there aren't any
     * context specific settings to be stored
     */
    private S extractContextSettings(S componentSettings, ComponentLifecycle<S> targetLifecycle) {
        return targetLifecycle.extractContextSettings((S) componentSettings);
    }

    /**
     * Stores the {@link Settings} of the provided {@link Component} in the default component settings tree. Make sure to
     * call this method only when {@link #hasMakeCustomDefaultSettingsSupport(Component)} method returns {@code true}
     * for the passed {@link Component}.
     * 
     * @param component
     *            The component which corresponds to the provided {@link Settings} 
     * @param newDefaultSettings
     *            The {@link Settings} to be stored
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        ComponentLifecycle<S> targetLifeCycle = determineLifecycle(component.getPath(), rootLifecycle);
        S globalSettings = extractGlobalSettings((S) newDefaultSettings, targetLifeCycle);
        S contextSettings = extractContextSettings((S) newDefaultSettings, targetLifeCycle);
        updateSettings(component.getPath(), globalSettings, contextSettings);
    }

    /**
     * Updates the settings tree with the new settings provided. The settings node with the specified path
     * is going to be created/replaced with the new settings.
     *  
     * @param root The root node of the settings tree
     * @param path The path of the node to create/update
     * @param newSettings The new settings with that the target node in the settings tree is going to be updated
     * @return
     */
    private JSONObject patchJsonObject(JSONObject root, List<String> path, Settings newSettings) {
        if(path.isEmpty()) {
            return (JSONObject) settingsStorageManager.settingsToJSON(newSettings);
        }
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
    private ComponentLifecycle<S> determineLifecycle(ArrayList<String> path, ComponentLifecycle<S> current) {
        while (current instanceof PerspectiveLifecycle<?> && !path.isEmpty()) {
            String last = path.remove(path.size() - 1);
            current = (ComponentLifecycle<S>) ((PerspectiveLifecycle) current).getLifecycleForId(last);
        }
        if (!path.isEmpty()) {
            throw new IllegalStateException("Settings path is not finished, but no perspective at current level");
        }
        return current;
    }
}
