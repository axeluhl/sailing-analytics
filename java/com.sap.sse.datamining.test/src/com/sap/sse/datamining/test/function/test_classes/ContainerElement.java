package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface ContainerElement {
    
    @Dimension(messageKey="name")
    public String getName();

}
