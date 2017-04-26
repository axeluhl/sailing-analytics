package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.settings.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;

/**
 * This {@link SettingsStorageManager} implementation only reads settings from the URL and does not support settings storage.
 * 
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 * @see SettingsStorageManager
 */
public class SimpleSettingsStorageManager<S extends Settings> implements SettingsStorageManager<S> {
    
    /**
     * The pipeline used for the settings construction.
     */
    protected final UrlSettingsBuildingPipeline settingsBuildingPipeline;
    
    /**
     * Constructs the instance with a {@link UrlSettingsBuildingPipeline}.
     */
    public SimpleSettingsStorageManager() {
        settingsBuildingPipeline = new UrlSettingsBuildingPipeline();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supportsStore() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveDefaultSettings(S defaultSettings, final OnSettingsLoadedCallback<S> asyncCallback) {
        asyncCallback.onSuccess(settingsBuildingPipeline.getSettingsObject(defaultSettings));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeGlobalSettingsJson(JSONObject globalSettingsJson, OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Settings storage is unsupported by SimpleSettingsStorageManager");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void storeContextSpecificSettingsJson(JSONObject contextSpecificSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback) {
        throw new UnsupportedOperationException("Settings storage is unsupported by SimpleSettingsStorageManager");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONValue settingsToJSON(Settings newSettings, PipelineLevel pipelineLevel, List<String> path) {
        throw new UnsupportedOperationException("Settings storage is unsupported by SimpleSettingsStorageManager");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void retrieveSettingsJsons(AsyncCallback<SettingsJsons> asyncCallback) {
        throw new UnsupportedOperationException("Settings storage is unsupported by SimpleSettingsStorageManager");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    }
}
