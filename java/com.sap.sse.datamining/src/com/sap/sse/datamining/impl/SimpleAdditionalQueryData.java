package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.Query.QueryType;

public class SimpleAdditionalQueryData implements AdditionalQueryData {
    
    private final QueryType type;
    private final UUID dataRetrieverChainID;
    
    public SimpleAdditionalQueryData(QueryType type, UUID dataRetrieverChainID) {
        this.type = type;
        this.dataRetrieverChainID = dataRetrieverChainID;
    }

    @Override
    public QueryType getType() {
        return type;
    }

    @Override
    public UUID getDataRetrieverChainID() {
        return dataRetrieverChainID;
    }

}
