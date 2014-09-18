package com.sap.sse.datamining.test.functions.registry.test_classes;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface Test_Named {
    
    @Dimension(messageKey="Name")
    public String getName();

}
