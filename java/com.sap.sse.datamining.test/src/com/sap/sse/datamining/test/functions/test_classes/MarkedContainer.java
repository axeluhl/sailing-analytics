package com.sap.sse.datamining.test.functions.test_classes;

import com.sap.sse.datamining.shared.annotations.Connector;

public interface MarkedContainer {
    
    @Connector
    public ContainerElement getContainerElement();

}
