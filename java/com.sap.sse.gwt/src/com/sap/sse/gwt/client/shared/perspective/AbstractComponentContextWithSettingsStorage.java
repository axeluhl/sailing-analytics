package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.components.Component;

public abstract class AbstractComponentContextWithSettingsStorage<PL extends PerspectiveLifecycle<PS>, PS extends Settings> extends AbstractComponentContext<PL, PS> {
    
    private final SettingsStorageManager<PS> settingsStorageManager;
    
    /**
     * Current default settings for the whole settings tree.
     */
    private PerspectiveCompositeSettings<PS> currentDefaultSettings = null;
    
    public AbstractComponentContextWithSettingsStorage(PL rootPerspectiveLifecycle, SettingsStorageManager<PS> settingsStorageManager) {
        super(rootPerspectiveLifecycle);
        this.settingsStorageManager = settingsStorageManager;
    }
    
    public void initDefaultSettings(final DefaultSettingsLoadedCallback<PS> asyncCallback) {
        if(currentDefaultSettings != null) {
            throw new IllegalStateException("Settings have been already initialized. You may only call this method once.");
        }
        PerspectiveCompositeSettings<PS> systemDefaultSettings = rootPerspectiveLifecycle.createDefaultSettings();
        settingsStorageManager.retrieveDefaultSettings(systemDefaultSettings, new DefaultSettingsLoadedCallback<PS>() {

            @Override
            public void onError(Throwable caught, PerspectiveCompositeSettings<PS> fallbackDefaultSettings) {
                asyncCallback.onError(caught, fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS> result) {
                currentDefaultSettings = rootPerspectiveLifecycle.cloneSettings(result);
                asyncCallback.onSuccess(result);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        Perspective<? extends Settings> parentPerspective = component.getComponentTreeNodeInfo().getParentPerspective();
        
        final PerspectiveCompositeSettings<PS> newRootPerspectiveSettings;
        if(parentPerspective == null) {
            if(component instanceof AbstractRootPerspectiveComposite) {
                //root perspective is updating its perspective own settings
                AbstractRootPerspectiveComposite<? extends PerspectiveLifecycle<PS>, PS> rootPerspective = (AbstractRootPerspectiveComposite<? extends PerspectiveLifecycle<PS>, PS>) component;
                newRootPerspectiveSettings = (PerspectiveCompositeSettings<PS>)newDefaultSettings;
                rootPerspective.getPerspectiveLifecycleWithAllSettings().setAllSettings(newRootPerspectiveSettings);
            } else {
                throw new IllegalStateException("Component must belong to a perspective when updating default settings");
            }
            
        } else {
            newRootPerspectiveSettings = (PerspectiveCompositeSettings<PS>) updatePerspectiveLifecycleWithAllSettings(parentPerspective, component, newDefaultSettings);
        }
        this.currentDefaultSettings = rootPerspectiveLifecycle.cloneSettings(newRootPerspectiveSettings);
        storeNewDefaultSettings(newRootPerspectiveSettings);
    }
    
    private void storeNewDefaultSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        PerspectiveCompositeSettings<PS> globalSettings = extractGlobalSettings(newRootPerspectiveSettings);
        if(globalSettings != null) {
            settingsStorageManager.storeGlobalSettings(globalSettings);
        }
        PerspectiveCompositeSettings<PS> contextSpecificSettings = extractContextSpecificSettings(newRootPerspectiveSettings);
        if(contextSpecificSettings != null) {
            settingsStorageManager.storeContextSpecificSettings(contextSpecificSettings);
        }
    }

    protected abstract PerspectiveCompositeSettings<PS> extractContextSpecificSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings);

    protected abstract PerspectiveCompositeSettings<PS> extractGlobalSettings(
            PerspectiveCompositeSettings<PS> newRootPerspectiveSettings);

    private<T extends Settings> PerspectiveCompositeSettings<?> updatePerspectiveLifecycleWithAllSettings(Perspective<T> perspective, Component<? extends Settings> replaceComponent, Settings replaceComponentNewDefaultSettings) {
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
            return updatePerspectiveLifecycleWithAllSettings(parentPerspective, perspective, allSettings);
        } else {
            return allSettings;
        }
    }
    
    public Throwable getLastError() {
        return settingsStorageManager.getLastError();
    }
    
    @Override
    public PerspectiveCompositeSettings<PS> getDefaultSettingsForRootPerspective() {
        if(currentDefaultSettings == null) {
            throw new IllegalStateException("Settings have not been initialized yet.");
        }
        return rootPerspectiveLifecycle.cloneSettings(currentDefaultSettings);
    }
    
    public<T> AsyncCallbackWithSettingsRetrievementJoiner<T, PS> createSettingsRetrievementWithAsyncCallbackJoiner(
            AsyncCallback<T> callbackToWrap) {
        return new AsyncCallbackWithSettingsRetrievementJoiner<>(this, callbackToWrap);
    }
    
    public static<PS1 extends Settings, PS2 extends Settings> void initMultipleDefaultSettings(AbstractComponentContextWithSettingsStorage<?, PS1> context1, AbstractComponentContextWithSettingsStorage<?, PS2> context2, final IOnDefaultSettingsLoaded onDefaultSettingsLoaded) {
        DoubleSettingsRetrievementJoiner<PS1, PS2> joiner = new DoubleSettingsRetrievementJoiner<PS1, PS2>(context1, context2) {
            @Override
            public void onAllDefaultSettingsLoaded() {
                onDefaultSettingsLoaded.onLoaded();
            }
        };
        joiner.startSettingsRetrievementAndJoinAsyncCallback();
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
