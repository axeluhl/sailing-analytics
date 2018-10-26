package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;

import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;

/** Instances of this class can load, update, create and remove stored data mining queries from the user store. */
public interface StoredDataMiningQueryPersister {

    /** @return all {@link StoredDataMiningQueryDTO}s the user has stored in his user store. */
    ArrayList<StoredDataMiningQueryDTO> retrieveStoredQueries();

    /** Updates or creates a new stored query and returns it. */
    StoredDataMiningQueryDTO updateOrCreateStoredQuery(StoredDataMiningQueryDTO query);

    /** Removes a stored query. */
    StoredDataMiningQueryDTO removeStoredQuery(StoredDataMiningQueryDTO query);

}