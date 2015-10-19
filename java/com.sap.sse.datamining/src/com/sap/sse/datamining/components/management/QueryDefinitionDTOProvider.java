package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionDTOProvider {
    
    Iterable<String> getNames();
    StatisticQueryDefinitionDTO get(String name);

}
