package com.sap.sse.datamining.ui.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.shiro.authz.UnauthorizedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.SerializationDummy;
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

    Date getComponentsChangedTimepoint() throws UnauthorizedException;

    FunctionDTO getIdentityFunction(String localeInfoName) throws UnauthorizedException;

    HashSet<FunctionDTO> getAllStatistics(String localeInfoName) throws UnauthorizedException;

    HashSet<FunctionDTO> getStatisticsFor(DataRetrieverChainDefinitionDTO retrieverChainDefinition,
            String localeInfoName) throws UnauthorizedException;

    HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitions(String localeInfoName)
            throws UnauthorizedException;

    HashSet<AggregationProcessorDefinitionDTO> getAggregatorDefinitionsFor(FunctionDTO extractionFunction,
            String localeInfoName) throws UnauthorizedException;

    HashSet<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO,
            String localeInfoName) throws UnauthorizedException;

    ReducedDimensionsDTO getReducedDimensionsMappedByLevelFor(
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName)
            throws UnauthorizedException;

    ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitions(String localeName)
            throws UnauthorizedException;

    ArrayList<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate,
            String localeInfoName) throws UnauthorizedException;

    QueryResultDTO<HashSet<Object>> getDimensionValuesFor(DataMiningSession session,
            DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, DataRetrieverLevelDTO retrieverLevel,
            HashSet<FunctionDTO> dimensionDTOs, HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings,
            HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelectionDTO,
            String localeInfoName) throws UnauthorizedException;

    <ResultType extends Serializable> QueryResultDTO<ResultType> runQuery(DataMiningSession session,
            ModifiableStatisticQueryDefinitionDTO queryDefinition) throws UnauthorizedException;

    HashSet<PredefinedQueryIdentifier> getPredefinedQueryIdentifiers() throws UnauthorizedException;

    ModifiableStatisticQueryDefinitionDTO getPredefinedQueryDefinition(PredefinedQueryIdentifier identifier,
            String localeInfoName) throws UnauthorizedException;

    <ResultType extends Serializable> QueryResultDTO<ResultType> runPredefinedQuery(DataMiningSession session,
            PredefinedQueryIdentifier identifier, String localeInfoName) throws UnauthorizedException;

    ModifiableStatisticQueryDefinitionDTO localize(ModifiableStatisticQueryDefinitionDTO queryDefinition,
            String localeInfoName) throws UnauthorizedException;

    SerializationDummy pseudoMethodSoThatSomeClassesAreAddedToTheGWTSerializationPolicy() throws UnauthorizedException;

    ArrayList<StoredDataMiningQueryDTOImpl> retrieveStoredQueries() throws UnauthorizedException;

    StoredDataMiningQueryDTOImpl updateOrCreateStoredQuery(StoredDataMiningQueryDTOImpl query)
            throws UnauthorizedException;

    StoredDataMiningQueryDTOImpl removeStoredQuery(StoredDataMiningQueryDTOImpl query) throws UnauthorizedException;

    ModifiableStatisticQueryDefinitionDTO getDeserializedQuery(String serializedQuery) throws UnauthorizedException;
}
