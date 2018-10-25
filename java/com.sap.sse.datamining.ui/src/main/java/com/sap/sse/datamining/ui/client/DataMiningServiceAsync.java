package com.sap.sse.datamining.ui.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.SerializationDummy;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;

public interface DataMiningServiceAsync {

    public void getComponentsChangedTimepoint(AsyncCallback<Date> asyncCallback);

    void getIdentityFunction(String localeInfoName, AsyncCallback<FunctionDTO> callback);
    
    void getAllStatistics(String localeInfoName, AsyncCallback<HashSet<FunctionDTO>> callback);

    void getStatisticsFor(DataRetrieverChainDefinitionDTO currentRetrieverChainDefinition, String localeName,
            AsyncCallback<HashSet<FunctionDTO>> asyncCallback);
    
    void getAggregatorDefinitions(String localeInfoName,
            AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>> callback);

    void getAggregatorDefinitionsFor(FunctionDTO extractionFunction, String localeInfoName,
            AsyncCallback<HashSet<AggregationProcessorDefinitionDTO>> asyncCallback);

    void getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName,
            AsyncCallback<HashSet<FunctionDTO>> callback);

    void getReducedDimensionsMappedByLevelFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO,
            String localeInfoName, AsyncCallback<ReducedDimensionsDTO> callback);

    void getDataRetrieverChainDefinitions(String localeInfoName,
            AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO>> asyncCallback);

    void getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName,
            AsyncCallback<ArrayList<DataRetrieverChainDefinitionDTO>> callback);

    void getDimensionValuesFor(DataMiningSession session,
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, DataRetrieverLevelDTO retrieverLevel,
            HashSet<FunctionDTO> dimensionDTOs, HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings,
            HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO,
            String localeInfoName, AsyncCallback<QueryResultDTO<HashSet<Object>>> callback);

    <ResultType extends Serializable> void runQuery(DataMiningSession session,
            StatisticQueryDefinitionDTO queryDefinition, AsyncCallback<QueryResultDTO<ResultType>> callback);

    void getPredefinedQueryIdentifiers(AsyncCallback<HashSet<PredefinedQueryIdentifier>> callback);
    
    void getPredefinedQueryDefinition(PredefinedQueryIdentifier identifier, String localeInfoName,
            AsyncCallback<StatisticQueryDefinitionDTO> callback);

    <ResultType extends Serializable> void runPredefinedQuery(DataMiningSession session,
            PredefinedQueryIdentifier identifier, String localeInfoName,
            AsyncCallback<QueryResultDTO<ResultType>> callback);
    
    void localize(StatisticQueryDefinitionDTO queryDefinition, String localeInfoName,
            AsyncCallback<StatisticQueryDefinitionDTO> callback);

    /**
     * This method does nothing, but is needed to ensure, that some classes for the data mining (like
     * {@link GenericGroupKey}) is added to the GWT serialization policy.<br />
     * This is necessary, because the type is somehow hidden from GWT. For Further information look at bug 1503.<br />
     */
    void pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy(AsyncCallback<SerializationDummy> callback);

    /** retrieves the {@link StoredDataMiningQueryDTO}s from the back end */
    void retrieveStoredQueries(AsyncCallback<ArrayList<StoredDataMiningQueryDTO>> callback);

    /** updates or creates a {@link StoredDataMiningQueryDTO} in the back end */
    void updateOrCreateStoredQuery(StoredDataMiningQueryDTO query, AsyncCallback<StoredDataMiningQueryDTO> callback);

    /** removes the {@link StoredDataMiningQueryDTO} if it exists from the back end */
    void removeStoredQuery(StoredDataMiningQueryDTO query, AsyncCallback<StoredDataMiningQueryDTO> callback);
}
