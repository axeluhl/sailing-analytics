package com.sap.sse.datamining;

import java.util.UUID;

import com.sap.sse.datamining.Query.QueryType;
import com.sap.sse.datamining.impl.NullAdditionalQueryData;

public interface AdditionalQueryData {

    public AdditionalQueryData NULL_INSTANCE = new NullAdditionalQueryData();

    public QueryType getType();

    public UUID getDataRetrieverChainID();

}