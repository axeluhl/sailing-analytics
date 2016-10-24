package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.defaultsettings.DefaultSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.defaultsettings.DefaultSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.AbstractRootPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;

public class ComponentContext<PS extends Settings> {
    
    private final DefaultSettingsStorage<PS> defaultSettingsStorage;
    private final PerspectiveLifecycle<PS> rootPerspectiveLifecycle;
    
    /**
     * Settings tree with defaults for all components.
     */
    private PerspectiveCompositeSettings<PS> currentDefaultSettings = null;
    
    public ComponentContext(PerspectiveLifecycle<PS> rootPerspectiveLifecycle) {
        this.rootPerspectiveLifecycle = rootPerspectiveLifecycle;
        this.defaultSettingsStorage = new DefaultSettingsStorage<>(rootPerspectiveLifecycle.getComponentId());
    }
    
    public void initDefaultSettings(final DefaultSettingsLoadedCallback<PS> asyncCallback) {
        if(currentDefaultSettings != null) {
            throw new IllegalStateException("Settings have been already initialized. You may only call this method once.");
        }
        PerspectiveCompositeSettings<PS> systemDefaultSettings = rootPerspectiveLifecycle.createDefaultSettings();
        defaultSettingsStorage.retrieveDefaultSettings(systemDefaultSettings, new DefaultSettingsLoadedCallback<PS>() {

            @Override
            public void onError(Throwable caught, PerspectiveCompositeSettings<PS> fallbackDefaultSettings) {
                asyncCallback.onError(caught, fallbackDefaultSettings);
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS> result) {
                currentDefaultSettings = result;
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
    
    private void storeNewDefaultSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        // TODO split settings in global settings and context specific
        defaultSettingsStorage.storeGlobalSettings(extractGlobalSettings(newRootPerspectiveSettings));
        defaultSettingsStorage.storeContextSpecificSettings(extractContextSpecificSettings(newRootPerspectiveSettings));
    }

    private PerspectiveCompositeSettings<PS> extractContextSpecificSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }

    private PerspectiveCompositeSettings<PS> extractGlobalSettings(
            PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        // TODO Auto-generated method stub
        return newRootPerspectiveSettings;
    }

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
    
}
