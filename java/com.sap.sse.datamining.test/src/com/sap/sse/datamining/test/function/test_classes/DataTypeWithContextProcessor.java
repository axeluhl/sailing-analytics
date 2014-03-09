package com.sap.sse.datamining.test.function.test_classes;

import com.sap.sse.datamining.annotations.Dimension;

public class DataTypeWithContextProcessor {
    
    @Dimension(messageKey="regattaAndRaceName")
    public String getRegattaAndRaceName(DataTypeWithContext dataEntry) {
        return dataEntry.getRegattaName() + " - " + dataEntry.getRaceName();
    }

}
