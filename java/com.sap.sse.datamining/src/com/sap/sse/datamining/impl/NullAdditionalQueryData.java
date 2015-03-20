package com.sap.sse.datamining.impl;

import java.util.UUID;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.Query.QueryType;

/**
 * Null Object pattern for {@link AdditionalQueryData} with the values:
 * <ul>
 *   <li><b>Type</b>: {@link QueryType#OTHER}</li>
 *   <li><b>Data Retriever Chain ID</b>: {@link UUID#UUID(long, long) UUID(0, 0)}</li>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 */
public class NullAdditionalQueryData implements AdditionalQueryData {
    
    private final UUID nullUUID = new UUID(0, 0);

    @Override
    public QueryType getType() {
        return QueryType.OTHER;
    }

    @Override
    public UUID getDataRetrieverChainID() {
        return nullUUID;
    }

}
