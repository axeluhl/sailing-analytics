package com.sap.sse.datamining.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface StatisticQueryDefinitionDTO extends Serializable {
    
    public DataRetrieverChainDefinitionDTO getDataRetrieverChainDefinition();
    /**
     * @return The settings to be used for the retriever levels. Can be empty, if no retriever level has settings.
     */
    public HashMap<DataRetrieverLevelDTO, SerializableSettings> getRetrieverSettings();
    /**
     * The values used to filter the data elements during the retrieval process. Each set of values is mapped by the
     * dimension used to extract the value from a data element and the retriever level the dimension belongs to. The
     * dimensions are declared by the data type retrieved by the corresponding retriever level and not by the data type
     * the query is based on (return type of the retriever chain).
     * 
     * The actual filter values can be anything that is returned by a dimension, which will be mostly simple data like
     * Strings or Enums, but can also be more complex data structures like {@link ClusterDTO}.
     * 
     * @return The values used to filter the data elements during the retrieval process.
     */
    public HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> getFilterSelection();
    public ArrayList<FunctionDTO> getDimensionsToGroupBy();
    public FunctionDTO getStatisticToCalculate();
    public AggregationProcessorDefinitionDTO getAggregatorDefinition();
    
    /**
     * @return The LocalInfo name that will be used for internationalization.
     */
    public String getLocaleInfoName();

}
