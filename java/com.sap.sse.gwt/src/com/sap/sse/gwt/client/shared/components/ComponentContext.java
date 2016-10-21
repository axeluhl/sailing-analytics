package com.sap.sse.gwt.client.shared.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.defaultsettings.DefaultSettingsStorage;
import com.sap.sse.gwt.client.shared.perspective.AbstractRootPerspectiveComposite;
import com.sap.sse.gwt.client.shared.perspective.Perspective;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycle;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveLifecycleWithAllSettings;

public class ComponentContext<PS extends Settings> {
    
    private final DefaultSettingsStorage<PS> defaultSettingsStorage;
    private final PerspectiveLifecycle<PS> rootPerspectiveLifecycle;
    private PerspectiveCompositeSettings<PS> currentDefaultSettings;
    
    //TODO add async callback propagation for initialization
    public ComponentContext(PerspectiveLifecycle<PS> rootPerspectiveLifecycle) {
        this.rootPerspectiveLifecycle = rootPerspectiveLifecycle;
        this.defaultSettingsStorage = new DefaultSettingsStorage<>(rootPerspectiveLifecycle.getComponentId());
        initDefaultSettings();
    }
    
    private void initDefaultSettings() {
        currentDefaultSettings = rootPerspectiveLifecycle.createDefaultSettings();
        defaultSettingsStorage.getDefaultSettings(currentDefaultSettings, new AsyncCallback<PerspectiveCompositeSettings<PS>>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(PerspectiveCompositeSettings<PS> result) {
                // TODO Auto-generated method stub
                
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
    
    //TODO use this method to get initial default settings instead of Lifecycle.createDefaultSettings,
    //or call it directly in Lifecycle.createDefaultSettings
    //or something different
    public<T extends Settings> T getDefaultSettingsForComponent(Component<T> component) {
        //TODO traverse settings tree to find component id with its settings
        T componentSettings = null;
        return componentSettings;
    }
    
//    @SuppressWarnings("unchecked")
//    public<S extends Settings> S getDefaultSettingsForComponent(Component<S> component) {
//        return (S) component.getComponentTreeNodeInfo().getParentPerspective().getSettings().findSettingsByComponentId(component.getId());
//    }
    
    private void storeNewDefaultSettings(PerspectiveCompositeSettings<PS> newRootPerspectiveSettings) {
        // TODO implement storage pipeline
        Window.alert("default settings are ready to enter the storage pipeline");
        defaultSettingsStorage.storeDefaultSettings(newRootPerspectiveSettings);
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
