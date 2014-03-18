package com.sap.sailing.gwt.ui.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.DataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface DataMiningService extends RemoteService {
    
    Collection<FunctionDTO> getDimensionsFor(DataTypes dataType);
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinition queryDefinition) throws Exception;

    DataMiningSerializationDummy pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy();

}
