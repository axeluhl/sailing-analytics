package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.shiro.authz.AuthorizationException;

import com.sap.sailing.server.impl.preferences.model.SailingPreferences;
import com.sap.sailing.server.impl.preferences.model.StoredDataMiningQueryPreference;
import com.sap.sailing.server.impl.preferences.model.StoredDataMiningQueryPreferences;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.shared.DataMiningQuerySerializer;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;
import com.sap.sse.gwt.dispatch.shared.exceptions.ServerDispatchException;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.User;
import com.sap.sse.security.UserStore;

public class StoredDataMiningQueryPersisterImpl implements StoredDataMiningQueryPersister {

    private final SecurityService securityService;
    private final UserStore userStore;

    public StoredDataMiningQueryPersisterImpl(SecurityService securityService, UserStore userStore) {
        this.securityService = securityService;
        this.userStore = userStore;

    }

    @Override
    public ArrayList<StoredDataMiningQueryDTO> retrieveStoredQueries() {

        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);
        if (prefs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(StreamSupport.stream(prefs.getStoredQueries().spliterator(), false).map(this::transform)
                .collect(Collectors.toList()));
    }

    @Override
    public StoredDataMiningQueryDTO updateOrCreateStoredQuery(StoredDataMiningQueryDTO query) {
        Collection<StoredDataMiningQueryPreference> updatedQueries = new ArrayList<>();
        updatedQueries.add(transform(query));

        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);
        if (prefs != null) {
            // copy existing preferences
            Iterable<StoredDataMiningQueryPreference> storedPrefs = prefs.getStoredQueries();

            // remove existing preference with the same UUID if it exists
            Iterator<StoredDataMiningQueryPreference> it = storedPrefs.iterator();
            while (it.hasNext()) {
                StoredDataMiningQueryPreference storedPreference = it.next();
                if (storedPreference.getId().equals(query.getId())) {
                    it.remove();
                    break;
                }
            }

            Util.addAll(storedPrefs, updatedQueries);
        }

        prefs = new StoredDataMiningQueryPreferences();
        prefs.setStoredQueries(updatedQueries);
        setPreferenceForCurrentUser(SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES, prefs);
        return query;
    }

    @Override
    public StoredDataMiningQueryDTO removeStoredQuery(StoredDataMiningQueryDTO query) {
        Collection<StoredDataMiningQueryPreference> updatedQueries = new ArrayList<>();

        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);
        if (prefs != null) {
            // copy existing preferences
            Iterable<StoredDataMiningQueryPreference> storedPrefs = prefs.getStoredQueries();

            // remove existing preference with the same UUID if it exists
            storedPrefs = StreamSupport.stream(storedPrefs.spliterator(), false)
                    .filter(q -> q.getId().equals(query.getId())).collect(Collectors.toList());

            Util.addAll(storedPrefs, updatedQueries);
        }

        prefs = new StoredDataMiningQueryPreferences();
        prefs.setStoredQueries(updatedQueries);
        setPreferenceForCurrentUser(SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES, prefs);
        return query;
    }

    private void setPreferenceForCurrentUser(String preferenceKey, Object preference) {
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            try {
                securityService.setPreferenceObject(currentUser.getName(), preferenceKey, preference);
            } catch (AuthorizationException e) {
                throw new ServerDispatchException(e);
            }
        }
    }

    private StoredDataMiningQueryDTO transform(StoredDataMiningQueryPreference pref) {
        StatisticQueryDefinitionDTO query = DataMiningQuerySerializer.fromBase64String(pref.getSerializedQuery());
        return new StoredDataMiningQueryDTOImpl(pref.getName(), pref.getId(), query);
    }

    private StoredDataMiningQueryPreference transform(StoredDataMiningQueryDTO dto) {
        String serializedQuery = DataMiningQuerySerializer.toBase64String(dto.getQuery());
        return new StoredDataMiningQueryPreference(dto.getName(), dto.getId(), serializedQuery);
    }

    private <T> T getPreferenceForCurrentUser(String preferenceKey) {
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            return userStore.getPreferenceObject(currentUser.getName(), preferenceKey);
        }
        return null;
    }

}
