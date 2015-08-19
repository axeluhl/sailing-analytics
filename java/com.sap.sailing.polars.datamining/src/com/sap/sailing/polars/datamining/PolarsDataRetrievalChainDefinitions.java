package com.sap.sailing.polars.datamining;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sse.datamining.components.DataRetrieverChainDefinition;

public class PolarsDataRetrievalChainDefinitions {
    
    private final Collection<DataRetrieverChainDefinition<?, ?>> dataRetrieverChainDefinitions;
    
    public PolarsDataRetrievalChainDefinitions() {
        dataRetrieverChainDefinitions = new ArrayList<>();
        //dataRetrieverChainDefinitions.add(e)
    }

    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions() {
        return dataRetrieverChainDefinitions;
    }

}
