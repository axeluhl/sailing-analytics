package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;

public interface DataMiningService extends RemoteService {

    Collection<FunctionDTO> getAllStatistics(String localeInfoName);
    
    Collection<FunctionDTO> getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName);
    Collection<FunctionDTO> getDimensionsFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinitionDTO, String localeInfoName);

    Collection<DataRetrieverChainDefinitionDTO> getDataRetrieverChainDefinitionsFor(FunctionDTO statisticToCalculate, String localeInfoName);

    QueryResult<Set<Object>> getDimensionValuesFor(DataRetrieverChainDefinitionDTO dataRetrieverChainDefinition, int retrieverLevel, Collection<FunctionDTO> dimensions, String localeInfoName) throws Exception;
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception;

    SSEDataMiningSerializationDummy pseudoMethodSoThatSomeSSEDataMiningClassesAreAddedToTheGWTSerializationPolicy();

    SailingDataMiningSerializationDummy pseudoMethodSoThatSomeSailingDataMiningClassesAreAddedToTheGWTSerializationPolicy();

}
