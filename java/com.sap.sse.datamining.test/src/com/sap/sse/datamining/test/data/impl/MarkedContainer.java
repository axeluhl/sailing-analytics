package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.annotations.Connector;

public interface MarkedContainer {
    
    @Connector
    public ContainerElement getContainerElement();

}
