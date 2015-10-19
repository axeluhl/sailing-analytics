package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.exceptions.DataMiningComponentAlreadyRegisteredForKeyException;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;

public interface QueryDefinitionDTORegistry extends QueryDefinitionDTOProvider {

    /**
     * Registers the given {@link StatisticQueryDefinitionDTO} for the given {@link PredefinedQueryIdentifier}.
     * @return <code>true</code>, if the registered query definitions have been changed.
     * @throws DataMiningComponentAlreadyRegisteredForKeyException
     *          If there's already a query definition registered for the given <code>identifier</code>.
     */
    boolean register(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * Removes the given {@link StatisticQueryDefinitionDTO} for the given {@link PredefinedQueryIdentifier}.
     * @return <code>true</code>, if the registered query definitions have been changed.
     */
    boolean unregister(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition);

}
