package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.shared.DataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface DataMiningService extends RemoteService {
    
    Collection<FunctionDTO> getDimensionsFor(DataTypes dataType);
    
    <ResultType extends Number> QueryResult<ResultType> runQuery(QueryDefinitionDeprecated queryDefinition) throws Exception;

    DataMiningSerializationDummy pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy();

}
