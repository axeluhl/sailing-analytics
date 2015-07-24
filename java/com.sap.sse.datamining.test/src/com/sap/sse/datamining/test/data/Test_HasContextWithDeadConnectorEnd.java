package com.sap.sse.datamining.test.data;

import com.sap.sse.datamining.functions.FunctionRegistry;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.datamining.test.domain.Test_DeadConnectorEnd;

/**
 * This class is used to test the case, that some as {@link Connector} annotated methods could
 * lead to nothing usable. This shouldn't occur, but could when Connectors were missed, after
 * some {@link Dimension Dimensions} or {@link Statistic Statistics} have been removed.
 * 
 * @author Lennart Hensler (D054527)
 */
public interface Test_HasContextWithDeadConnectorEnd {
    
    /**
     *  This "connection" method doesn't lead to anything usable (like Dimensions or Statistics),
     *  so the methods of {@link Test_DeadConnectorEnd} (which aren't annotated)
     *  should be ignored by {@link FunctionRegistry FunctionRegistries}.
     */
    @Connector
    public Test_DeadConnectorEnd getDeadConnectorEnd();

}
