package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;
import com.sap.sse.common.settings.generic.SettingsMap;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsLoadedCallback;
import com.sap.sse.gwt.client.shared.perspective.OnSettingsStoredCallback;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;
import com.sap.sse.gwt.client.shared.perspective.SettingsStorageManager;
import com.sap.sse.gwt.settings.SettingsToUrlSerializer;

/**
 * This {@link SettingsStorageManager} implementation only reads settings from the URL.
 * 
 * @param <S>
 *            The {@link Settings} type of the settings of the root component/perspective containing all the settings
 *            for itself and its subcomponents
 * @see SettingsStorageManager
 */
public class SimpleSettingsStorageManager<S extends Settings> implements SettingsStorageManager<S> {
    private final SettingsToUrlSerializer urlSerializer = new SettingsToUrlSerializer();

    @Override
    public boolean supportsStore() {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public void retrieveDefaultSettings(S defaultSettings, final OnSettingsLoadedCallback<S> asyncCallback) {
        asyncCallback.onSuccess(deserializeFromCurrentUrl(defaultSettings));
    }

    @SuppressWarnings("unchecked")
    protected S deserializeFromCurrentUrl(S defaultSettings) {
        if (defaultSettings instanceof GenericSerializableSettings) {
            defaultSettings = (S) urlSerializer
                    .deserializeFromCurrentLocation((GenericSerializableSettings) defaultSettings);
        } else if (defaultSettings instanceof SettingsMap) {
            defaultSettings = (S) urlSerializer
                    .deserializeSettingsMapFromCurrentLocation((SettingsMap) defaultSettings);
        }
        return defaultSettings;
    }

    @Override
    public void storeSettingsJsons(SettingsJsons settingsJsons, OnSettingsStoredCallback onSettingsStoredCallback) {
        
    }

    @Override
    public Throwable getLastError() {
        return null;
    }

    @Override
    public JSONValue settingsToJSON(Settings newSettings) {
        return null;
    }

    @Override
    public void retrieveSettingsJson(AsyncCallback<SettingsJsons> asyncCallback) {
        
    }

    @Override
    public void dispose() {
    }
}
