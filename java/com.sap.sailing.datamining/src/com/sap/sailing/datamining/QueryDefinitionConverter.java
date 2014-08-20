package com.sap.sailing.datamining;

import java.io.Serializable;
import java.util.Map.Entry;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.QueryDefinitionImpl;

public class QueryDefinitionConverter {

    public static QueryDefinition convertDeprecatedQueryDefinition(QueryDefinitionDeprecated queryDefinitionDeprecated) {
        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl(queryDefinitionDeprecated.getLocaleInfoName(),
                queryDefinitionDeprecated.getStatisticToCalculate(),
                queryDefinitionDeprecated.getAggregatorType());

        for (Entry<DimensionIdentifier, Iterable<? extends Serializable>> filterSelectionEntry : queryDefinitionDeprecated.getSelection()
                .entrySet()) {
            queryDefinition.setFilterSelectionFor(
                    DeprecatedEnumsToFunctionDTOConverter.getFunctionDTOFor(filterSelectionEntry.getKey()),
                    filterSelectionEntry.getValue());
        }

        for (FunctionDTO dimension : queryDefinitionDeprecated.getDimensionsToGroupBy()) {
            queryDefinition.appendDimensionToGroupBy(dimension);
        }

        return queryDefinition;
    }

    private QueryDefinitionConverter() {
    }

}
