package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public interface DataMiningService extends RemoteService {

    Date getComponentsChangedTimepoint();

    Iterable<FunctionDTO> getAllStatistics(String localeInfoName);
    
    Iterable<FunctionDTO> getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName);
    Iterable<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName);

    Iterable<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName);

    QueryResult<Set<Object>> getDimensionValuesFor(DataMiningSession session, DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, int retrieverLevel,
            Iterable<FunctionDTO> dimensionDTOs, Map<Integer, Map<FunctionDTO, Collection<?>>> filterSelectionDTO, String localeInfoName);
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(DataMiningSession session, StatisticQueryDefinitionDTO queryDefinition);

    SSEDataMiningSerializationDummy pseudoMethodSoThatSomeSSEDataMiningClassesAreAddedToTheGWTSerializationPolicy();

    SailingDataMiningSerializationDummy pseudoMethodSoThatSomeSailingDataMiningClassesAreAddedToTheGWTSerializationPolicy();

}
