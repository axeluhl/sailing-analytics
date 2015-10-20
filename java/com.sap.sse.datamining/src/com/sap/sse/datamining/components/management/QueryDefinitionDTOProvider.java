package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public interface QueryDefinitionDTOProvider {
    
    Iterable<PredefinedQueryIdentifier> getIdentifiers();
    ModifiableStatisticQueryDefinitionDTO get(PredefinedQueryIdentifier identifier);

}
