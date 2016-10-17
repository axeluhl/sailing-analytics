package com.sap.sse.gwt.client.shared.components;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Window;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.defaultsettings.DefaultSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;

public class ComponentContext {
    
    private final Perspective<? extends Settings> rootPerspective;
    private final DefaultSettingsStorage defaultSettingsStorage = new DefaultSettingsStorage(this);
    
    public ComponentContext(Perspective<? extends Settings> rootPerspective) {
        this.rootPerspective = rootPerspective;
    }
    
    public void makeSettingsDefault(Component<? extends Settings> component, Settings newDefaultSettings) {
        Perspective<? extends Settings> parentPerspective = component.getComponentTreeNodeInfo().getParentPerspective();
        if(parentPerspective == null) {
            throw new IllegalStateException("Component must belong to a perspective when updating default settings");
        }
        PerspectiveCompositeSettings<?> newRootPerspectiveSettings = updatePerspectiveLifecycleWithAllSettings(parentPerspective, component, newDefaultSettings);
        storeNewDefaultSettings(newRootPerspectiveSettings);
    }
    
    private void storeNewDefaultSettings(PerspectiveCompositeSettings<?> newRootPerspectiveSettings) {
        // TODO implement storage pipeline
        Window.alert("default settings are ready to enter the storage pipeline");
        defaultSettingsStorage.storeDefaultSettings();
    }

    private<S extends Settings> PerspectiveCompositeSettings<?> updatePerspectiveLifecycleWithAllSettings(Perspective<S> perspective, Component<? extends Settings> replaceComponent, Settings replaceComponentNewDefaultSettings) {
        PerspectiveLifecycleWithAllSettings<?,S> perspectiveLifecycleWithAllSettings = perspective.getPerspectiveLifecycleWithAllSettings();
        Map<Serializable, Settings> originalSettingsPerComponent = perspectiveLifecycleWithAllSettings.getComponentSettings().getSettingsPerComponentId();
        Map<Serializable, Settings> newSettingsPerComponent = new HashMap<>();
        for (Entry<Serializable, Settings> entry : originalSettingsPerComponent.entrySet()) {
            Serializable componentId = entry.getKey();
            if(replaceComponent.getId().equals(componentId)) {
                newSettingsPerComponent.put(replaceComponent.getId(), replaceComponentNewDefaultSettings);
            } else {
                newSettingsPerComponent.put(componentId, entry.getValue());
            }
        }
        
        PerspectiveCompositeSettings<S> allSettings = new PerspectiveCompositeSettings<>(perspectiveLifecycleWithAllSettings.getPerspectiveSettings(), newSettingsPerComponent);
        perspectiveLifecycleWithAllSettings.setAllSettings(allSettings);
        
        Perspective<? extends Settings> parentPerspective = perspective.getComponentTreeNodeInfo().getParentPerspective();
        if(parentPerspective != null) {
            return updatePerspectiveLifecycleWithAllSettings(parentPerspective, perspective, allSettings);
        } else {
            return allSettings;
        }
    }
    
    public Perspective<? extends Settings> getRootPerspective() {
        return rootPerspective;
    }

}
