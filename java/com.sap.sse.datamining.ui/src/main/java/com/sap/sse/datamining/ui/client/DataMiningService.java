package com.sap.sse.datamining.ui.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.SerializationDummy;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.dto.StoredDataMiningQueryDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.shared.impl.dto.ReducedDimensionsDTO;
import com.sap.sse.datamining.shared.impl.dto.StoredDataMiningQueryDTOImpl;

public interface DataMiningService extends RemoteService {

    Date getComponentsChangedTimepoint();

    FunctionDTO getIdentityFunction(String localeInfoName);
    HashSet<FunctionDTO> getAllStatistics(String localeInfoName);

    HashSet<FunctionDTO> getStatisticsFor(DataRetrieverChainDefinitionDTO retrieverChainDefinition,
            String localeInfoName);
    
    HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitions(String localeInfoName);

    HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitionsFor(FunctionDTO extractionFunction,
            String localeInfoName);

    HashSet<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO,
            String localeInfoName);

    ReducedDimensionsDTO getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName);

    ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitions(String localeName);

    ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate,
            String localeInfoName);

    QueryResultDTO<HashSet<Object>> getDimensionValuesFor(DataMiningSession session,
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, DataRetrieverLevelDTO retrieverLevel,
            HashSet<FunctionDTO> dimensionDTOs, HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings,
            HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO,
            String localeInfoName);

    <ResultType extends Serializable> QueryResultDTO<ResultType> runQuery(DataMiningSession session,
            StatisticQueryDefinitionDTO queryDefinition);

    HashSet<PredefinedQueryIdentifier> getPredefinedQueryIdentifiers();
    
    StatisticQueryDefinitionDTO getPredefinedQueryDefinition(PredefinedQueryIdentifier identifier, String localeInfoName);

    <ResultType extends Serializable> QueryResultDTO<ResultType> runPredefinedQuery(DataMiningSession session,
            PredefinedQueryIdentifier identifier, String localeInfoName);
    
    StatisticQueryDefinitionDTO localize(StatisticQueryDefinitionDTO queryDefinition, String localeInfoName);

    SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy();

    ArrayList<StoredDataMiningQueryDTO> retrieveStoredQueries();

    StoredDataMiningQueryDTO updateOrCreateStoredQuery(StoredDataMiningQueryDTO query);

    StoredDataMiningQueryDTO removeStoredQuery(StoredDataMiningQueryDTO query);

    StatisticQueryDefinitionDTO getDeserializedQuery(String serializedQuery);
    
    Pair<ModifiableStatisticQueryDefinitionDTO, StoredDataMiningQueryDTOImpl> serializationDummy(
            ModifiableStatisticQueryDefinitionDTO query, StoredDataMiningQueryDTOImpl storedQuery);
    
}
