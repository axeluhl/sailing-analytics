package com.sap.sse.datamining.test.functions.test_classes;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface ContainerElement {
    
    @Dimension(messageKey="Name")
    public String getName();

}
