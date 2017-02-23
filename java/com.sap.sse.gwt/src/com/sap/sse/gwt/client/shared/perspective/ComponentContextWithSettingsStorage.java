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
public class ComponentContextWithSettingsStorage<S extends Settings> extends SimpleComponentContext<S> {

    /**
     * Manages the persistence layer of settings.
     */
    private final SettingsStorageManager<S> settingsStorageManager;

    /**
     * 
     * @param rootLifecycle
     *            The {@link ComponentLifecycle} of the root component/perspective
     * @param settingsStorageManager
     *            The {@link SettingsStorageManager} to be used access stored settings and store new settings
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
     * {@inheritDoc}
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

    @SuppressWarnings("unchecked")
    public S extractGlobalSettings(Component<? extends Settings> component, Settings newDefaultSettings) {
        ComponentLifecycle<S> targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        return extractGlobalSettings((S) newDefaultSettings, targetLifeCycle);
    }

    @SuppressWarnings("unchecked")
    public S extractContextSettings(Component<? extends Settings> component, Settings newDefaultSettings) {
        ComponentLifecycle<S> targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        return extractContextSettings((S) newDefaultSettings, targetLifeCycle);
    }

    private S extractGlobalSettings(S newDefaultSettings, ComponentLifecycle<S> targetLifeCycle) {
        return targetLifeCycle.extractGlobalSettings((S) newDefaultSettings);
    }

    private S extractContextSettings(S newDefaultSettings, ComponentLifecycle<S> targetLifeCycle) {
        return targetLifeCycle.extractContextSettings((S) newDefaultSettings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        ComponentLifecycle<S> targetLifeCycle = determineLifeCycle(component.getPath(), rootLifecycle);
        S globalSettings = extractGlobalSettings((S) newDefaultSettings, targetLifeCycle);
        S contextSettings = extractContextSettings((S) newDefaultSettings, targetLifeCycle);
        updateSettings(component.getPath(), globalSettings, contextSettings);
    }

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
    private ComponentLifecycle<S> determineLifeCycle(ArrayList<String> path, ComponentLifecycle<S> current) {
        while (current instanceof PerspectiveLifecycle<?> && !path.isEmpty()) {
            String last = path.remove(path.size() - 1);
            current = (ComponentLifecycle<S>) ((PerspectiveLifecycle) current).getLiveCycleForId(last);
        }
        if (!path.isEmpty()) {
            throw new IllegalStateException("Settings path is not finished, but no perspective at current level");
        }
        return current;
    }
}
