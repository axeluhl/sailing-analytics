package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.SerializationDummy;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public interface DataMiningServiceAsync {

    public void getComponentsChangedTimepoint(AsyncCallback<Date> asyncCallback);

    void getAllStatistics(String localeInfoName, AsyncCallback<Iterable<FunctionDTO>> callback);
    void getStatisticsFor(DataRetrieverChainDefinitionDTO currentRetrieverChainDefinition, String localeName,
            AsyncCallback<Iterable<FunctionDTO>> asyncCallback);

    void getAggregatorDefinitionsFor(FunctionDTO extractionFunction, String localeInfoName,
            AsyncCallback<Iterable<AggregationProcessorDefinitionDTO>> asyncCallback);

    void getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName, AsyncCallback<Iterable<FunctionDTO>> callback);
    void getDimensionsMappedByLevelFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName,
            AsyncCallback<HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>>> callback);
    public void getReducedDimensionsMappedByLevelFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName,
            AsyncCallback<HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>>> callback);
    
    void getDataRetrieverChainDefinitions(String localeInfoName, AsyncCallback<Iterable<DataRetrieverChainDefinitionDTO>> asyncCallback);
    void getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName,
            AsyncCallback<Iterable<DataRetrieverChainDefinitionDTO>> callback);

    void getDimensionValuesFor(DataMiningSession session, DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, int retrieverLevel,
            Iterable<FunctionDTO> dimensionDTOs, Map<Integer, Map<FunctionDTO, Collection<?>>> filterSelectionDTO,
            String localeInfoName, AsyncCallback<QueryResultDTO<Set<Object>>> callback);

    <ResultType> void runQuery(DataMiningSession session, StatisticQueryDefinitionDTO queryDefinition, AsyncCallback<QueryResultDTO<ResultType>> callback);
    
    /**
     * This method does nothing, but is needed to ensure, that some classes for the data mining
     * (like {@link GenericGroupKey}) is added to the GWT serialization policy.<br />
     * This is necessary, because the type is somehow hidden from GWT. For Further information
     * look at bug 1503.<br />
     */
    void pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy(AsyncCallback<SerializationDummy> callback);

}
