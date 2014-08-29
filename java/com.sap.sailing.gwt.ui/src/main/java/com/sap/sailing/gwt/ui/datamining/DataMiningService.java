package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface DataMiningService extends RemoteService {

    Collection<FunctionDTO> getAllStatistics(String localeInfoName);
    
    Collection<FunctionDTO> getDimensionsFor(FunctionDTO statisticToCalculate, String localeInfoName);

    Object getDimensionValuesFor(Collection<FunctionDTO> dimensions);
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinitionDeprecated queryDefinition) throws Exception;

    SSEDataMiningSerializationDummy pseudoMethodSoThatSomeSSEDataMiningClassesAreAddedToTheGWTSerializationPolicy();

    SailingDataMiningSerializationDummy pseudoMethodSoThatSomeSailingDataMiningClassesAreAddedToTheGWTSerializationPolicy();

}
