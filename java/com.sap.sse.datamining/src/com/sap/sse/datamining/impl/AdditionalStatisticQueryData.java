package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.Query.QueryType;

public class AdditionalStatisticQueryData extends AbstractAdditionalQueryData {
    
    public AdditionalStatisticQueryData(UUID dataRetrieverChainID) {
        super(QueryType.STATISTIC, dataRetrieverChainID);
    }

}
