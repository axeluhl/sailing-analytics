package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;

/**
 * This {@link SettingsStorageManager} implementation only reads settings from the URL.
 * 
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 * @see SettingsStorageManager
 */
public class SimpleSettingsStorageManager<S extends Settings> implements SettingsStorageManager<S> {
    
    protected final SettingsBuildingPipeline settingsBuildingPipeline;
    
    public SimpleSettingsStorageManager() {
        this(new UrlSettingsBuildingPipeline());
    }
    
    public SimpleSettingsStorageManager(SettingsBuildingPipeline settingsBuildingPipeline) {
        this.settingsBuildingPipeline = settingsBuildingPipeline;
    }

    @Override
    public boolean supportsStore() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public void retrieveDefaultSettings(S defaultSettings, final OnSettingsLoadedCallback<S> asyncCallback) {
        asyncCallback.onSuccess(settingsBuildingPipeline.getSettingsObject(defaultSettings));
    }

    @Override
    public void storeSettingsJsons(SettingsJsons settingsJsons, OnSettingsStoredCallback onSettingsStoredCallback) {
    }
    
    @Override
    public void storeGlobalSettingsJson(JSONObject globalSettingsJson, OnSettingsStoredCallback onSettingsStoredCallback) {
    }
    
    @Override
    public void storeContextSpecificSettingsJson(JSONObject contextSpecificSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback) {
    }

    @Override
    public JSONValue settingsToJSON(Settings newSettings) {
        return null;
    }

    @Override
    public void retrieveSettingsJsons(AsyncCallback<SettingsJsons> asyncCallback) {
    }
    
    @Override
    public void retrieveGlobalSettingsJson(AsyncCallback<JSONObject> asyncCallback) {
    }
    
    @Override
    public void retrieveContextSpecificSettingsJson(AsyncCallback<JSONObject> asyncCallback) {
    }

    @Override
    public void dispose() {
    }
}
