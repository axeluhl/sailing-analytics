package com.sap.sse.datamining;

import java.util.Map;

import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

/**
 * A Service that provides predefined {@link StatisticQueryDefinitionDTO}s of a data mining bundle.
 * A data mining bundle has to register such a service to the context. Such registrations are
 * automatically tracked by the domain independent data mining bundle, which calls the methods
 * an integrates the predefined queries in the data mining framework.
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public interface DataMiningPredefinedQueryService {
    
    /**
     * @return The predefined {@link StatisticQueryDefinitionDTO}s mapped by their name.
     */
    public Map<String, StatisticQueryDefinitionDTO> getPredefinedQueries();

}
