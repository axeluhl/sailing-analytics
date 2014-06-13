package com.sap.sailing.datamining;

import java.util.Map.Entry;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.impl.QueryDefinitionImpl;

public class QueryDefinitionConverter {

    public static QueryDefinition convertDeprecatedQueryDefinition(QueryDefinitionDeprecated queryDefinitionDeprecated) {
        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl(queryDefinitionDeprecated.getLocaleInfoName(),
                DeprecatedEnumsToFunctionDTOConverter.getFunctionDTOFor(queryDefinitionDeprecated.getStatisticType()),
                queryDefinitionDeprecated.getAggregatorType());

        for (Entry<DimensionIdentifier, Iterable<?>> filterSelectionEntry : queryDefinitionDeprecated.getSelection()
                .entrySet()) {
            queryDefinition.setFilterSelectionFor(
                    DeprecatedEnumsToFunctionDTOConverter.getFunctionDTOFor(filterSelectionEntry.getKey()),
                    filterSelectionEntry.getValue());
        }

        for (DimensionIdentifier dimensionIdentifier : queryDefinitionDeprecated.getDimensionsToGroupBy()) {
            queryDefinition.appendDimensionToGroupBy(DeprecatedEnumsToFunctionDTOConverter
                    .getFunctionDTOFor(dimensionIdentifier));
        }

        return queryDefinition;
    }

    private QueryDefinitionConverter() {
    }

}
