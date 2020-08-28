package com.sap.sse.datamining.ui.client;

import org.apache.shiro.authz.UnauthorizedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;

public interface DataMiningWriteService extends RemoteService {

    StoredDataMiningQueryDTOImpl updateOrCreateStoredQuery(StoredDataMiningQueryDTOImpl query)
            throws UnauthorizedException;

    StoredDataMiningQueryDTOImpl removeStoredQuery(StoredDataMiningQueryDTOImpl query) throws UnauthorizedException;
}
