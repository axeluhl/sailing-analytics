package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface ContainerElement {
    
    @Dimension(messageKey="Name")
    public String getName();

}
