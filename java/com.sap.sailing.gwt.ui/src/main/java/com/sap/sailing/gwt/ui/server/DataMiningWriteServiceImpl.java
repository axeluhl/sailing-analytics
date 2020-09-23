package com.sap.sailing.gwt.ui.server;

import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;
import com.sap.sse.datamining.ui.client.DataMiningWriteService;

public class DataMiningWriteServiceImpl extends DataMiningServiceImpl implements DataMiningWriteService {
    private static final long serialVersionUID = -414648611878838551L;

    @Override
    public StoredDataMiningQueryDTOImpl updateOrCreateStoredQuery(StoredDataMiningQueryDTOImpl query) {
        checkDataMiningPermission();
        return (StoredDataMiningQueryDTOImpl) storedDataMiningQueryPersistor.updateOrCreateStoredQuery(query);
    }

    @Override
    public StoredDataMiningQueryDTOImpl removeStoredQuery(StoredDataMiningQueryDTOImpl query) {
        checkDataMiningPermission();
        return (StoredDataMiningQueryDTOImpl) storedDataMiningQueryPersistor.removeStoredQuery(query);
    }

}
