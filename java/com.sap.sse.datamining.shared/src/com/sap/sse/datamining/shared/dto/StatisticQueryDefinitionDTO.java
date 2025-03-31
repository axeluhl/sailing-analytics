package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticQueryDefinitionDTO extends Serializable {

    DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();

    /**
     * @return The settings to be used for the retriever levels. Can be empty, if no retriever level has settings.
     */
    HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings();

    /**
     * The values used to filter the data elements during the retrieval process. Each set of values is mapped by the
     * dimension used to extract the value from a data element and the retriever level the dimension belongs to. The
     * dimensions are declared by the data type retrieved by the corresponding retriever level and not by the data type
     * the query is based on (return type of the retriever chain).
     * 
     * The actual filter values can be anything that is returned by a dimension, which will be mostly simple data like
     * Strings or Enums, but can also be more complex data structures like {@link ClusterDTO}. The values of the value
     * maps hence must conform with the key dimension function's {@link FunctionDTO#getReturnTypeName() return type}.
     * 
     * @return The values used to filter the data elements during the retrieval process as a non-live copy
     */
    HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getFilterSelection();
    
    default <T extends Serializable> Iterable<T> getFilterSelection(FilterDimensionIdentifier filterDimensionIdentifier) {
        final Set<T> result = new HashSet<>();
        final HashMap<FunctionDTO, HashSet<? extends Serializable>> filtersAtRetrieverLevel = getFilterSelection().get(filterDimensionIdentifier.getRetrieverLevel());
        if (filtersAtRetrieverLevel != null) {
            @SuppressWarnings("unchecked")
            final HashSet<T> filterValuesForDimension = (HashSet<T>) filtersAtRetrieverLevel.get(filterDimensionIdentifier.getDimensionFunction());
            if (filterValuesForDimension != null) {
                result.addAll(filterValuesForDimension);
            }
        }
        return result;
    }

    ArrayList<FunctionDTO> getDimensionsToGroupBy();

    FunctionDTO getStatisticToCalculate();

    AggregationProcessorDefinitionDTO getAggregatorDefinition();

    /**
     * @return The LocalInfo name that will be used for internationalization.
     */
    String getLocaleInfoName();

    boolean isQueryChangedSinceLastRun();
    
    void setQueryChangedSinceLastRun(boolean queryChangedSinceLastRun);
}
