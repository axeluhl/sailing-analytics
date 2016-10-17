package com.sap.sse.gwt.client.shared.defaultsettings;

import com.sap.sse.gwt.client.shared.components.ComponentContext;

public class DefaultSettingsStorage {
    
    private final ComponentContext componentContext;
    
//    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();

    public DefaultSettingsStorage(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }
    
    public void storeDefaultSettings() {
        //TODO how to use serializer with PerspectiveCompositeSettings?
        //iterate over components and call their getSettings() method.
        //components should get also a method getGuiState()
//        componentContext.getRootPerspective()
    }
    
    public void getDefaultSettings() {
        //TODO where to retrieve settings from?
        
        //create system default
        //obtain my default from localStorage
        //obtain my default from server
        
        //rules
        //context specific
        
        //obtain from url
    }
    
    public void generateShareSettingsLink() {
        //TODO how to use serializer with PerspectiveCompositeSettings?
//      componentContext.getRootPerspective().getPerspectiveLifecycleWithAllSettings();
        
        //use settings from root perspective?
    }

}
