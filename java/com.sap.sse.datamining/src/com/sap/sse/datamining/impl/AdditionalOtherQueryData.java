package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.Query.QueryType;

public class AdditionalOtherQueryData extends AbstractAdditionalQueryData {

    public AdditionalOtherQueryData(UUID dataRetrieverChainID) {
        super(QueryType.OTHER, dataRetrieverChainID);
    }

}
