package com.sap.sailing.gwt.ui.datamining.client;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.shared.DataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public interface DataMiningServiceAsync {

    void getDimensionsFor(DataTypes dataType, AsyncCallback<Collection<FunctionDTO>> callback);

    <ResultType extends Number> void runQuery(QueryDefinitionDeprecated queryDefinition, AsyncCallback<QueryResult<ResultType>> callback);
    
    /**
     * This method does nothing, but is needed to ensure, that GenericGroupKey&ltString&gt in the GWT serialization policy.<br />
     * This is necessary, because the type is somehow covered from GWT. For Further information look at bug 1503.<br />
     */
    void pseudoMethodSoThatSomeDataMiningClassesAreAddedToTheGWTSerializationPolicy(AsyncCallback<DataMiningSerializationDummy> asyncCallback);

}
