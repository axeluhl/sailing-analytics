package com.sap.sse.datamining.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;

public interface DataMiningWriteServiceAsync {

    /** Updates or creates a {@link StoredDataMiningQueryDTO} in the back end. */
    void updateOrCreateStoredQuery(StoredDataMiningQueryDTOImpl query,
            AsyncCallback<StoredDataMiningQueryDTOImpl> callback);

    /** Removes the {@link StoredDataMiningQueryDTO} if it exists from the back end. */
    void removeStoredQuery(StoredDataMiningQueryDTOImpl query, AsyncCallback<StoredDataMiningQueryDTOImpl> callback);

}
