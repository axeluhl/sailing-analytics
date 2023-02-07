package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.stream.StreamSupport;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public interface DataMiningReportDTO extends Serializable {
    Iterable<StatisticQueryDefinitionDTO> getQueryDefinitions();
    
    Iterable<FilterDimensionParameter> getParameters();

    Iterable<Pair<StatisticQueryDefinitionDTO, FilterDimensionIdentifier>> getParameterUsages(FilterDimensionParameter parameter);
    
    FilterDimensionParameter getParameter(StatisticQueryDefinitionDTO query, FilterDimensionIdentifier filterDimensionIdentifier);
    
    /**
     * Filters all {@link #getParameters() parameters} defined in this report by their
     * {@link FilterDimensionParameter#getTypeName() type name}. This shall allow a caller to find appropriate existing
     * parameters that may be used for binding them to a dimension filter.
     * 
     * @return a one-time iterable, never {@code null} but possibly empty
     */
    default Iterable<FilterDimensionParameter> getParametersForTypeName(final String typeName) {
        return StreamSupport.stream(getParameters().spliterator(), /* parallel */ false)
                .filter(parameter->Util.equalsWithNull(parameter.getTypeName(), typeName))
                ::iterator;
    }
}
