package com.sap.sse.gwt.client.shared.defaultsettings;

import java.io.Serializable;

import com.google.gwt.storage.client.Storage;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.gwt.client.shared.components.ComponentContext;
import com.sap.sse.gwt.settings.SettingsToJsonSerializerGWT;

public class DefaultSettingsStorage {
    
    private final String storageRootPerspectiveKey;
    
    //TODO which service to use for server calls? Where to get?
    //private XYService service...
    
    private final ComponentContext componentContext;
    
//    private final SettingsToJsonSerializer serializer = new SettingsToJsonSerializer();
    
    private GenericSerializableSettings rootPerspectiveDefaultSettings = null;

    public DefaultSettingsStorage(ComponentContext componentContext) {
        this.componentContext = componentContext;
        Serializable rootPerspectiveId = componentContext.getRootPerspective().getId();
        //TODO make id string, not serializable
        storageRootPerspectiveKey = rootPerspectiveId.toString();
    }
    
    public void storeDefaultSettings() {
        //TODO how to use serializer with PerspectiveCompositeSettings?
        //iterate over components and call their getSettings() method.
        //components should get also a method getGuiState()
//        componentContext.getRootPerspective()
    }
    
    public GenericSerializableSettings getDefaultSettings() {
        if(rootPerspectiveDefaultSettings == null) {
            //TODO retrieve settings
            
            //create system default
            //obtain my default from localStorage
            //obtain my default from server
            
            //rules
            //context specific
            
            //obtain from url
        }
        return rootPerspectiveDefaultSettings;
    }
    
    public void generateShareSettingsLink() {
        //TODO how to use serializer with PerspectiveCompositeSettings?
//      componentContext.getRootPerspective().getPerspectiveLifecycleWithAllSettings();
        
        //use settings from root perspective?
    }
    
    private void storeDefaultSettingsInLocalStorage() {
        //TODO add module dependency to LocalStorage
        //TODO get root settings
        GenericSerializableSettings settings = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            // delete old value
            localStorage.removeItem(storageRootPerspectiveKey);
            // store settings
            localStorage.setItem(storageRootPerspectiveKey,
                    new SettingsToJsonSerializerGWT().serializeToString(settings));
        }
    }

    private<T extends GenericSerializableSettings> T loadDefaultSettingsFromLocalStorage(T systemDefaultSettings) {
        T loadedSettings = null;
        Storage localStorage = Storage.getLocalStorageIfSupported();
        if (localStorage != null) {
            String jsonAsLocalStore = localStorage.getItem(storageRootPerspectiveKey);
            loadedSettings = new SettingsToJsonSerializerGWT().deserialize(systemDefaultSettings, jsonAsLocalStore);
        }
        return loadedSettings;
    }

}
