package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.exceptions.DataMiningComponentAlreadyRegisteredForKeyException;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public interface QueryDefinitionDTORegistry extends QueryDefinitionDTOProvider {

    /**
     * Registers the given {@link StatisticQueryDefinitionDTO} for the given <code>name</code>.
     * @return <code>true</code>, if the registered query definitions have been changed.
     * @throws DataMiningComponentAlreadyRegisteredForKeyException
     *          If there's already a query definition registered for the given <code>name</code>.
     */
    boolean register(String name, StatisticQueryDefinitionDTO queryDefinition);
    
    /**
     * Removes the given {@link StatisticQueryDefinitionDTO} for the given <code>name</code>.
     * @return <code>true</code>, if the registered query definitions have been changed.
     */
    boolean unregister(String name, StatisticQueryDefinitionDTO queryDefinition);

}
