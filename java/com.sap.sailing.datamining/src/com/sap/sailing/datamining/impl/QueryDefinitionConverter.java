package com.sap.sailing.datamining.impl;

import java.util.Map.Entry;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.impl.QueryDefinitionImpl;

public class QueryDefinitionConverter {

    public static QueryDefinition convertDeprecatedQueryDefinition(QueryDefinitionDeprecated queryDefinitionDeprecated) {
        QueryDefinitionImpl queryDefinition = new QueryDefinitionImpl(queryDefinitionDeprecated.getLocaleInfoName(),
                DeprecatedToFunctionConverter.getFunctionDTOFor(queryDefinitionDeprecated.getStatisticType()),
                queryDefinitionDeprecated.getAggregatorType());

        for (Entry<DimensionIdentifier, Iterable<?>> filterSelectionEntry : queryDefinitionDeprecated.getSelection()
                .entrySet()) {
            queryDefinition.setFilterSelectionFor(
                    DeprecatedToFunctionConverter.getFunctionDTOFor(filterSelectionEntry.getKey()),
                    filterSelectionEntry.getValue());
        }

        for (DimensionIdentifier dimensionIdentifier : queryDefinitionDeprecated.getDimensionsToGroupBy()) {
            queryDefinition.appendDimensionToGroupBy(DeprecatedToFunctionConverter
                    .getFunctionDTOFor(dimensionIdentifier));
        }

        return queryDefinition;
    }

    private QueryDefinitionConverter() {
    }

}
