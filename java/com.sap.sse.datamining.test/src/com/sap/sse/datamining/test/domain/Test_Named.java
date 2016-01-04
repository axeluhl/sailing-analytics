package com.sap.sse.datamining.test.domain;

import com.sap.sse.datamining.annotations.Dimension;

public interface Test_Named {
    
    @Dimension(messageKey="Name")
    public String getName();

}
