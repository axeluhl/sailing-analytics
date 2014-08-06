package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.datamining.shared.SailingDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.datamining.shared.SSEDataMiningSerializationDummy;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;

public interface DataMiningServiceAsync {

    void getDimensionsFor(DataTypes dataType, AsyncCallback<Collection<FunctionDTO>> callback);

    <ResultType extends Number> void runQuery(QueryDefinitionDeprecated queryDefinition, AsyncCallback<QueryResult<ResultType>> callback);
    
    /**
     * This method does nothing, but is needed to ensure, that some classes for the data mining
     * (like {@link GenericGroupKey}) is added to the GWT serialization policy.<br />
     * This is necessary, because the type is somehow hidden from GWT. For Further information
     * look at bug 1503.<br />
     */
    void pseudoMethodSoThatSomeSSEDataMiningClassesAreAddedToTheGWTSerializationPolicy(AsyncCallback<SSEDataMiningSerializationDummy> asyncCallback);
    
    /**
     * This method does nothing, but is needed to ensure, that some classes for the data mining
     * (like {@link GenericGroupKey}) is added to the GWT serialization policy.<br />
     * This is necessary, because the type is somehow hidden from GWT. For Further information
     * look at bug 1503.<br />
     */
    void pseudoMethodSoThatSomeSailingDataMiningClassesAreAddedToTheGWTSerializationPolicy(AsyncCallback<SailingDataMiningSerializationDummy> asyncCallback);

}
