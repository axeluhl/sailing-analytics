package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.shared.annotations.Connector;

public interface MarkedContainer {
    
    @Connector
    public ContainerElement getContainerElement();

}
