package com.sap.sse.gwt.client.shared.perspective;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.components.Component;

public abstract class ComponentContext<PL extends PerspectiveLifecycle<PS>, PS extends Settings> {
    
    private final SettingsStorageManager<PS> settingsStorageManager;
    private final PL rootPerspectiveLifecycle;
    
    /**
     * Settings tree with defaults for all components.
     */
    private PerspectiveCompositeSettings<PS> currentDefaultSettings = null;
    
    private PerspectiveLifecycleWithAllSettings<PL, PS> rootPerspectiveLifecycleWithCurrentDefaultSettings = null;
    
    public ComponentContext(String entryPointId, PL rootPerspectiveLifecycle, String...contextDefinitionParameters) {
        this.rootPerspectiveLifecycle = rootPerspectiveLifecycle;
        this.settingsStorageManager = new SettingsStorageManager<>(entryPointId + "+" + rootPerspectiveLifecycle.getComponentId(), buildContextDefinitionId(contextDefinitionParameters));
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
                currentDefaultSettings = result;
                rootPerspectiveLifecycleWithCurrentDefaultSettings = new PerspectiveLifecycleWithAllSettings<>(rootPerspectiveLifecycle, result);
                //assure, the defaults are not modified outside of context
                asyncCallback.onSuccess(rootPerspectiveLifecycle.cloneSettings(result));
            }
        });
    }

    @SuppressWarnings("unchecked")
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
        storeNewDefaultSettings(newRootPerspectiveSettings);
    }
    
    public PL getRootPerspectiveLifecycle() {
        return rootPerspectiveLifecycle;
    }
    
    public PerspectiveLifecycleWithAllSettings<PL, PS> getRootPerspectiveLifecycleWithCurrentDefaultSettings() {
        return rootPerspectiveLifecycleWithCurrentDefaultSettings;
    }
    
    private void storeNewDefaultSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        // TODO split settings in global settings and context specific
        settingsStorageManager.storeGlobalSettings(extractGlobalSettings(newRootPerspectiveSettings));
        settingsStorageManager.storeContextSpecificSettings(extractContextSpecificSettings(newRootPerspectiveSettings));
    }

    protected abstract PerspectiveCompositeSettings<PS> extractContextSpecificSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings);

    protected abstract PerspectiveCompositeSettings<PS> extractGlobalSettings(
            PerspectiveCompositeSettings<PS> newRootPerspectiveSettings);

    private<T extends Settings> PerspectiveCompositeSettings<?> updatePerspectiveLifecycleWithAllSettings(Perspective<T> perspective, Component<? extends Settings> replaceComponent, Settings replaceComponentNewDefaultSettings) {
        PerspectiveLifecycleWithAllSettings<?,T> perspectiveLifecycleWithAllSettings = perspective.getPerspectiveLifecycleWithAllSettings();
        Map<String, Settings> originalSettingsPerComponent = perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsPerComponentId();
        Map<String, Settings> newSettingsPerComponent = new HashMap<>();
        for (Entry<String, Settings> entry : originalSettingsPerComponent.entrySet()) {
            String componentId = entry.getKey();
            if(replaceComponent.getId().equals(componentId)) {
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
    
    public Throwable getLastServerError() {
        return settingsStorageManager.getLastServerError();
    }
    
    public PerspectiveCompositeSettings<PS> getDefaultSettingsForRootPerspective() {
        return rootPerspectiveLifecycle.cloneSettings(currentDefaultSettings);
    }
    
    private static String buildContextDefinitionId(String[] contextDefinitionParameters) {
        StringBuilder str = new StringBuilder("<");
        for(String contextDefinitionParameter : contextDefinitionParameters) {
            if(contextDefinitionParameter != null) {
                str.append(contextDefinitionParameter);
            }
            str.append(";");
        }
        str.append(">");
        return str.toString();
    }

    public<T> AsyncCallbackWithSettingsRetrievementJoiner<T, PS> createAsyncCallbackJoiner(
            AsyncCallback<T> callbackToWrap) {
        return new AsyncCallbackWithSettingsRetrievementJoiner<>(this, callbackToWrap);
    }
    
    public static<PS1 extends Settings, PS2 extends Settings> void initMultipleDefaultSettings(ComponentContext<?, PS1> context1, ComponentContext<?, PS2> context2, final IOnDefaultSettingsLoaded onDefaultSettingsLoaded) {
        DoubleSettingsRetrievementJoiner<PS1, PS2> joiner = new DoubleSettingsRetrievementJoiner<PS1, PS2>(context1, context2) {
            @Override
            public void onAllDefaultSettingsLoaded() {
                onDefaultSettingsLoaded.onLoaded();
            }
        };
        joiner.startSettingsRetrievementAndJoinAsyncCallback();
    }
    
}
