package com.sap.sailing.gwt.ui.server;

import java.util.ArrayList;

import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;

public interface StoredDataMiningQueryPersister {

    ArrayList<StoredDataMiningQueryDTO> retrieveStoredQueries();

    StoredDataMiningQueryDTO updateOrCreateStoredQuery(StoredDataMiningQueryDTO query);

    StoredDataMiningQueryDTO removeStoredQuery(StoredDataMiningQueryDTO query);

}