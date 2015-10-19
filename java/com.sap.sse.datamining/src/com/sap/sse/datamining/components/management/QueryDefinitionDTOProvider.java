package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;

public interface QueryDefinitionDTOProvider {
    
    Iterable<PredefinedQueryIdentifier> getIdentifiers();
    StatisticQueryDefinitionDTO get(PredefinedQueryIdentifier identifier);

}
