package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;
import com.sap.sse.security.ui.settings.StoredSettingsLocation;

/**
 * @param <C>
 *            the provided client factory type
 */
public class UserSettingsPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService>
        implements UserSettingsView.Presenter {

    private final C clientFactory;
    private UserSettingsView view;

    public UserSettingsPresenter(C clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadData() {
        if(clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            clientFactory.getUserService().getPreferenceswithPrefix(SailingSettingsConstants.USER_SETTINGS_UI, new AsyncCallback<Map<String,String>>() {
                @Override
                public void onSuccess(Map<String, String> result) {
                    loadSettingsFromLocalStorage(result);
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    clientFactory.createErrorView("Error while loading user settings!", caught);
                }
            });
        } else {
            loadSettingsFromLocalStorage(Collections.emptyMap());
        }
    }
    
    private void loadSettingsFromLocalStorage(final Map<String, String> userSettings) {
        final Map<String, String> localSettings = loadSettingsFromLocalStorage();
        final Set<String> allKeys = new TreeSet<>((a, b) -> a.compareTo(b));
        allKeys.addAll(userSettings.keySet());
        allKeys.addAll(localSettings.keySet());
        final List<UserSettingsEntry> entries = new ArrayList<>(allKeys.size());
        for(String key : allKeys) {
            final String keyWithoutContext, documentSettingsId;
            final int separatorIndex = key.indexOf(StoredSettingsLocation.DOCUMENT_SETTINGS_SUFFIX_SEPARATOR);
            if(separatorIndex > 0) {
                keyWithoutContext = key.substring(0, separatorIndex);
                documentSettingsId = key.substring(separatorIndex + 1);
            } else {
                keyWithoutContext = key;
                documentSettingsId = null;
            }
            entries.add(new UserSettingsEntry(keyWithoutContext, documentSettingsId, userSettings.get(key), localSettings.get(key)));
        }
        view.setEntries(entries);
    }
    
    private Map<String, String> loadSettingsFromLocalStorage() {
        final Map<String, String> localSettings;
        if(Storage.isLocalStorageSupported()) {
            localSettings = new HashMap<String, String>();
            Storage localStorage = Storage.getLocalStorageIfSupported();
            for(int i = 0; i< localStorage.getLength(); i++) {
                String key = localStorage.key(i);
                if(key.startsWith(SailingSettingsConstants.USER_SETTINGS_UI)) {
                    String value = localStorage.getItem(key);
                    localSettings.put(key, value);
                }
            }
        } else {
            localSettings = Collections.emptyMap();
        }
        return localSettings;
    }

    @Override
    public void remove(UserSettingsEntry entry) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setView(UserSettingsView view) {
        this.view = view;
    }

}
