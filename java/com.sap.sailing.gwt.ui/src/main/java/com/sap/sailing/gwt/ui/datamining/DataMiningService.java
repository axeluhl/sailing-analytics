package com.sap.sailing.gwt.ui.datamining;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.SerializationDummy;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public interface DataMiningService extends RemoteService {

    Date getComponentsChangedTimepoint();

    HashSet<FunctionDTO> getAllStatistics(String localeInfoName);
    HashSet<FunctionDTO> getStatisticsFor(DataRetrieverChainDefinitionDTO<Settings> retrieverChainDefinition, String localeInfoName);

    HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitionsFor(FunctionDTO extractionFunction, String localeInfoName);
    
    HashSet<FunctionDTO> getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName);
    HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> getDimensionsMappedByLevelFor(
            DataRetrieverChainDefinitionDTO<Settings> dataRetrieverChainDefinitionDTO, String localeInfoName);
    HashMap<DataRetrieverLevelDTO, HashSet<FunctionDTO>> getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinitionDTO<Settings> dataRetrieverChainDefinitionDTO, String localeInfoName);

    ArrayList<DataRetrieverChainDefinitionDTO<Settings>> getDataRetrieverChainDefinitions(String localeName);
    ArrayList<DataRetrieverChainDefinitionDTO<Settings>> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName);

    QueryResultDTO<HashSet<Object>> getDimensionValuesFor(DataMiningSession session, DataRetrieverChainDefinitionDTO<Settings> dataRetrieverChainDefinitionDTO, DataRetrieverLevelDTO retrieverLevel,
            HashSet<FunctionDTO> dimensionDTOs, HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO, String localeInfoName);
    
    <ResultType> QueryResultDTO<ResultType> runQuery(DataMiningSession session, StatisticQueryDefinitionDTO queryDefinition);

    SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy();

}
