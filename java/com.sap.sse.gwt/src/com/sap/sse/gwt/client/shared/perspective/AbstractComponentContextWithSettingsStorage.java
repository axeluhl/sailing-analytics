package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public abstract class AbstractComponentContextWithSettingsStorage<L extends ComponentLifecycle<S, ?>, S extends Settings> extends AbstractComponentContext<L, S> {
    
    private final SettingsStorageManager<S> settingsStorageManager;
    
    private Queue<SettingsReceiverCallback<S>> settingsReceiverCallbacks = new LinkedList<>();
    
    /**
     * Current default settings for the whole settings tree.
     */
    private S currentDefaultSettings = null;
    
    public AbstractComponentContextWithSettingsStorage(L rootLifecycle, SettingsStorageManager<S> settingsStorageManager) {
        super(rootLifecycle);
        this.settingsStorageManager = settingsStorageManager;
    }
    
    public void initInitialSettings() {
        initInitialSettings(null);
    }
    
    public void initInitialSettings(final OnSettingsLoadedCallback<S> onInitialSettingsLoaded) {
        if(currentDefaultSettings != null) {
            throw new IllegalStateException("Settings have been already initialized. You may only call this method once.");
        }
        S systemDefaultSettings = rootLifecycle.createDefaultSettings();
        settingsStorageManager.retrieveDefaultSettings(systemDefaultSettings, new OnSettingsLoadedCallback<S>() {

            @Override
            public void onError(Throwable caught, S fallbackDefaultSettings) {
                if(onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onError(caught, fallbackDefaultSettings);
                }
            }

            @Override
            public void onSuccess(S result) {
                currentDefaultSettings = result;
                if(onInitialSettingsLoaded != null) {
                    onInitialSettingsLoaded.onSuccess(rootLifecycle.cloneSettings(result));
                }
                SettingsReceiverCallback<S> callback;
                while((callback = settingsReceiverCallbacks.poll()) != null) {
                    callback.receiveSettings(rootLifecycle.cloneSettings(result));
                }
            }
        });
    }
    
    public void receiveInitialSettings(SettingsReceiverCallback<S> settingsReceiverCallback) {
        if(currentDefaultSettings == null) {
            settingsReceiverCallbacks.add(settingsReceiverCallback);
        } else {
            settingsReceiverCallback.receiveSettings(currentDefaultSettings);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        Perspective<? extends Settings> parentPerspective = component.getComponentTreeNodeInfo().getParentPerspective();
        
        final S newRootSettings;
        if(parentPerspective == null) {
            if(component instanceof Perspective) {
                //root perspective is updating its perspective own settings
                Perspective<? extends Settings> rootPerspective = (Perspective<? extends Settings>) component;
                setPerspectiveSettingsHelper(rootPerspective, newDefaultSettings);
                newRootSettings = (S) newDefaultSettings;
            } else {
                //root is a single component
                newRootSettings = (S) newDefaultSettings;
            }
        } else {
            // propagate updated settings to parent and get root settings
            newRootSettings = (S) propagateSettingsToRootPerspective(parentPerspective, component, newDefaultSettings);
        }
        this.currentDefaultSettings = rootLifecycle.cloneSettings(newRootSettings);
        storeNewDefaultSettings(newRootSettings);
    }
    
    private static<PS extends Settings> void setPerspectiveSettingsHelper(Perspective<PS> perspective, Settings newPerspectiveSettings) {
        @SuppressWarnings("unchecked")
        PerspectiveCompositeSettings<PS> newRootSettings = (PerspectiveCompositeSettings<PS>)newPerspectiveSettings;
        perspective.getPerspectiveLifecycleWithAllSettings().setAllSettings(newRootSettings);
    }
    
    private void storeNewDefaultSettings(S newRootSettings) {
        S globalSettings = extractGlobalSettings(newRootSettings);
        if(globalSettings != null) {
            settingsStorageManager.storeGlobalSettings(globalSettings);
        }
        S contextSpecificSettings = extractContextSpecificSettings(newRootSettings);
        if(contextSpecificSettings != null) {
            settingsStorageManager.storeContextSpecificSettings(contextSpecificSettings);
        }
    }

    protected abstract S extractContextSpecificSettings(S newRootSettings);

    protected abstract S extractGlobalSettings(
            S newRootSettings);

    private<T extends Settings> PerspectiveCompositeSettings<?> propagateSettingsToRootPerspective(Perspective<T> perspective, Component<? extends Settings> replaceComponent, Settings replaceComponentNewDefaultSettings) {
        PerspectiveLifecycleWithAllSettings<?,T> perspectiveLifecycleWithAllSettings = perspective.getPerspectiveLifecycleWithAllSettings();
        Map<String, Settings> originalSettingsPerComponent = perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsPerComponentId();
        Map<String, Settings> newSettingsPerComponent = new HashMap<>();
        String replaceComponentId = replaceComponent.getId();
        for (Entry<String, Settings> entry : originalSettingsPerComponent.entrySet()) {
            String componentId = entry.getKey();
            if(replaceComponentId.equals(componentId)) {
                newSettingsPerComponent.put(replaceComponent.getId(), replaceComponentNewDefaultSettings);
            } else {
                newSettingsPerComponent.put(componentId, entry.getValue());
            }
        }
        
        PerspectiveCompositeSettings<T> allSettings = new PerspectiveCompositeSettings<>(perspectiveLifecycleWithAllSettings.getPerspectiveSettings(), newSettingsPerComponent);
        perspectiveLifecycleWithAllSettings.setAllSettings(allSettings);
        
        Perspective<? extends Settings> parentPerspective = perspective.getComponentTreeNodeInfo().getParentPerspective();
        if(parentPerspective != null) {
            return propagateSettingsToRootPerspective(parentPerspective, perspective, allSettings);
        } else {
            return allSettings;
        }
    }
    
    public Throwable getLastError() {
        return settingsStorageManager.getLastError();
    }
    
    @Override
    public S getDefaultSettings() {
        if(currentDefaultSettings == null) {
            throw new IllegalStateException("Settings have not been initialized yet.");
        }
        return rootLifecycle.cloneSettings(currentDefaultSettings);
    }
    
    @Override
    public boolean hasMakeCustomDefaultSettingsSupport(Component<?> component) {
        if(!component.hasSettings()) {
            return false;
        }
        Settings settings = component.getSettings();
        if(settings instanceof SettingsMap || settings instanceof GenericSerializableSettings) {
            return true;
        }
        return false;
    }
    
}
