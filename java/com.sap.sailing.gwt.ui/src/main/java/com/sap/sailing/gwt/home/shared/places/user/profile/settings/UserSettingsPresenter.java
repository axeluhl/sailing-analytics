package com.sap.sailing.gwt.home.shared.places.user.profile.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithDispatch;
import com.sap.sailing.gwt.ui.client.refresh.ErrorAndBusyClientFactory;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.gwt.client.xdstorage.CrossDomainStorage;
import com.sap.sse.security.ui.authentication.WithAuthenticationManager;
import com.sap.sse.security.ui.authentication.WithUserService;

/**
 * @param <C>
 *            the provided client factory type
 */
public class UserSettingsPresenter<C extends ClientFactoryWithDispatch & ErrorAndBusyClientFactory & WithAuthenticationManager & WithUserService>
        implements UserSettingsView.Presenter {

    private final C clientFactory;
    private UserSettingsView view;

    private final List<UserSettingsEntry> currentlyShownEntries = new ArrayList<>();

    public UserSettingsPresenter(C clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadData() {
        if (clientFactory.getAuthenticationManager().getAuthenticationContext().isLoggedIn()) {
            clientFactory.getUserService().getAllPreferences(new AsyncCallback<Map<String,String>>() {
                @Override
                public void onSuccess(Map<String, String> result) {
                    loadSettingsFromLocalStorage(result, clientFactory.getUserService().getStorage());
                }
                
                @Override
                public void onFailure(Throwable caught) {
                    clientFactory.createErrorView("Error while loading user settings!", caught);
                }
            });
        } else {
            loadSettingsFromLocalStorage(Collections.emptyMap(), clientFactory.getUserService().getStorage());
        }
    }
    
    @Override
    public void updateData() {
        view.setEntries(currentlyShownEntries.stream().filter(view.getFilter()::matches).collect(Collectors.toList()));
    }

    private void loadSettingsFromLocalStorage(final Map<String, String> userSettings, CrossDomainStorage storage) {
        loadSettingsFromLocalStorage(storage, localSettings->{
            final Set<String> allKeys = new TreeSet<>((a, b) -> a.compareTo(b));
            allKeys.addAll(userSettings.keySet());
            allKeys.addAll(localSettings.keySet());
            currentlyShownEntries.clear();
            for (String key : allKeys) {
                currentlyShownEntries.add(new UserSettingsEntry(key, userSettings.get(key), localSettings.get(key)));
            }
            this.updateData();
        });
    }
    
    private void loadSettingsFromLocalStorage(CrossDomainStorage storage, Consumer<Map<String, String>> resultCallback) {
        GWT.debugger(); // TODO bug5048: debug this to see if it still works
        final Map<String, String> localSettings = new HashMap<String, String>();
        storage.getLength(length->{
            int[] numberOfRequestsSent = new int[1];
            int[] numberOfResponsesReceived = new int[1];
            for (int i=0; i<length; i++) {
                storage.key(i, key->{
                    if (key.startsWith(SailingSettingsConstants.USER_SETTINGS_UI)) {
                        numberOfRequestsSent[0]++;
                        storage.getItem(key, value->{
                            localSettings.put(key, value);
                            numberOfResponsesReceived[0]++;
                            if (numberOfResponsesReceived[0] == numberOfRequestsSent[0]) {
                                resultCallback.accept(localSettings);
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void remove(UserSettingsEntry entry) {
        String storageKey = entry.getKey();
        clientFactory.getUserService().unsetPreference(storageKey);
        clientFactory.getUserService().getStorage().removeItem(storageKey, /* callback */ null);
        currentlyShownEntries.remove(entry);
        view.setEntries(currentlyShownEntries);
    }

    @Override
    public void setView(UserSettingsView view) {
        this.view = view;
    }

}
