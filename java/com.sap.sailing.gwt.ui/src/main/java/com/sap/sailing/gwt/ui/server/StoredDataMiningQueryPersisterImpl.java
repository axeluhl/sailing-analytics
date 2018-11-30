package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;
import java.util.Collection;
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

/** Implementation of {@link StoredDataMiningQueryPersister}. */
public class StoredDataMiningQueryPersisterImpl implements StoredDataMiningQueryPersister {

    private final SecurityService securityService;

    public StoredDataMiningQueryPersisterImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    /** @return all {@link StoredDataMiningQueryDTO}s the user has stored in his user store. */
    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<StoredDataMiningQueryDTOImpl> retrieveStoredQueries() {

        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);
        if (prefs == null) {
            return new ArrayList<>();
        }
        return new ArrayList<StoredDataMiningQueryDTOImpl>(
                (Collection<? extends StoredDataMiningQueryDTOImpl>) StreamSupport
                        .stream(prefs.getStoredQueries().spliterator(), false).map(this::transform)
                .collect(Collectors.toList()));
    }

    /** Updates or creates a new stored query and returns it. */
    @Override
    public StoredDataMiningQueryDTO updateOrCreateStoredQuery(StoredDataMiningQueryDTO query) {

        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);

        // remove query
        Collection<StoredDataMiningQueryPreference> updatedQueries = removeQueryFromIterable(query, prefs);

        updatedQueries.add(transform(query));
        prefs = new StoredDataMiningQueryPreferences();
        prefs.setStoredQueries(updatedQueries);
        setPreferenceForCurrentUser(SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES, prefs);
        return query;
    }

    /** Removes a stored query from the user store and returns it. */
    @Override
    public StoredDataMiningQueryDTO removeStoredQuery(StoredDataMiningQueryDTO query) {
        StoredDataMiningQueryPreferences prefs = getPreferenceForCurrentUser(
                SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES);

        // remove query
        Collection<StoredDataMiningQueryPreference> updatedQueries = removeQueryFromIterable(query, prefs);

        // update preferences
        prefs = new StoredDataMiningQueryPreferences();
        prefs.setStoredQueries(updatedQueries);
        setPreferenceForCurrentUser(SailingPreferences.STORED_DATAMINING_QUERY_PREFERENCES, prefs);
        return query;
    }

    /** Remove a query from an iterable by filtering the UUIDs of all queries in the iterable. */
    private Collection<StoredDataMiningQueryPreference> removeQueryFromIterable(StoredDataMiningQueryDTO query,
            StoredDataMiningQueryPreferences prefs) {
        if (prefs != null) {
            // copy existing preferences
            Iterable<StoredDataMiningQueryPreference> storedPrefs = prefs.getStoredQueries();

            // remove existing preference with the same UUID if it exists
            storedPrefs = StreamSupport.stream(storedPrefs.spliterator(), false)
                    .filter(q -> !q.getId().equals(query.getId())).collect(Collectors.toList());

            Collection<StoredDataMiningQueryPreference> updatedEntries = new ArrayList<>();
            Util.addAll(storedPrefs, updatedEntries);
            return updatedEntries;
        }
        return new ArrayList<>();
    }

    /** Sets a preference for the current user. */
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

    /** Converts a {@link StoredDataMiningQueryPreference} to a {@link StoredDataMiningQueryDTO}. */
    private StoredDataMiningQueryDTO transform(StoredDataMiningQueryPreference pref) {
        StatisticQueryDefinitionDTO query = DataMiningQuerySerializer.fromBase64String(pref.getSerializedQuery());
        return new StoredDataMiningQueryDTOImpl(pref.getName(), pref.getId(), query);
    }

    /** Converts a {@link StoredDataMiningQueryDTO} to a {@link StoredDataMiningQueryPreference}. */
    private StoredDataMiningQueryPreference transform(StoredDataMiningQueryDTO dto) {
        String serializedQuery = DataMiningQuerySerializer.toBase64String(dto.getQuery());
        return new StoredDataMiningQueryPreference(dto.getName(), dto.getId(), serializedQuery);
    }

    /** @return the preference for the current user associated with {@link preferenceKey} */
    private <T> T getPreferenceForCurrentUser(String preferenceKey) {
        User currentUser = securityService.getCurrentUser();
        if (currentUser != null) {
            return securityService.getPreferenceObject(currentUser.getName(), preferenceKey);
        }
        return null;
    }
}
