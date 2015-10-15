package com.sap.sse.datamining.impl.components.management;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.datamining.components.management.QueryDefinitionDTOProvider;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class QueryDefinitionDTOManager implements QueryDefinitionDTOProvider {

    private final Map<String, StatisticQueryDefinitionDTO> definitionDTOsMappedByName;
    
    public QueryDefinitionDTOManager() {
        definitionDTOsMappedByName = new HashMap<>();
    }

    @Override
    public Iterable<String> getNames() {
        return definitionDTOsMappedByName.keySet();
    }

    @Override
    public StatisticQueryDefinitionDTO get(String name) {
        return definitionDTOsMappedByName.get(name);
    }

}
