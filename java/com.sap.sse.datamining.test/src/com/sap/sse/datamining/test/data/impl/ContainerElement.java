package com.sap.sse.datamining.test.data.impl;

import com.sap.sse.datamining.annotations.Dimension;

public interface ContainerElement {
    
    @Dimension(messageKey="Name")
    public String getName();

    @Dimension(messageKey="StringFromInt")
    public String getStringFromInt(int i);
}
