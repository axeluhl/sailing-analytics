package com.sap.sailing.datamining.shared;

import java.util.Map.Entry;

import com.sap.sailing.datamining.shared.impl.QueryDefinitionImpl;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class QueryDefinitionConverter {

    public static QueryDefinition convertDeprecatedQueryDefinition(QueryDefinitionDeprecated queryDefinitionDeprecated) {
        //TODO implement the statistic type to function conversion
        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl(queryDefinitionDeprecated.getLocaleInfoName(),
                convertToFunctionDTO(queryDefinitionDeprecated.getStatisticType()), queryDefinitionDeprecated.getAggregatorType());

        for (Entry<DimensionIdentifier, Iterable<?>> filterSelectionEntry : queryDefinitionDeprecated.getSelection().entrySet()) {
            //TODO implement the dimension identifier to function conversion
            queryDefinition.setFilterSelectionFor(convertToFunctionDTO(filterSelectionEntry.getKey()),
                                                  filterSelectionEntry.getValue());
        }
        
        for (DimensionIdentifier dimensionIdentifier : queryDefinitionDeprecated.getDimensionsToGroupBy()) {
            queryDefinition.appenDimensionToGroupBy(convertToFunctionDTO(dimensionIdentifier));
        }

        return queryDefinition;
    }

    private static FunctionDTO convertToFunctionDTO(StatisticType statisticType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not yet implemented");
//        return null;
    }

    private static FunctionDTO convertToFunctionDTO(DimensionIdentifier dimensionIdentifier) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Not yet implemented");
        // return null;
    }

    private QueryDefinitionConverter() {
    }

}
