package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.AdditionalQueryData;
import com.sap.sse.datamining.Query.QueryType;

/**
 * Null Object pattern for {@link AdditionalQueryData} with the values:
 * <ul>
 *   <li><b>Type</b>: {@link QueryType#OTHER}</li>
 * </ul>
 * 
 * @author Lennart Hensler (D054527)
 */
public class NullAdditionalQueryData implements AdditionalQueryData {
    
    @Override
    public QueryType getType() {
        return QueryType.OTHER;
    }

}
