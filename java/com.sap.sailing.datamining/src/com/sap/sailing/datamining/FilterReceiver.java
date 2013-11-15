package com.sap.sailing.datamining;

import java.util.Collection;

public interface FilterReceiver<DataType> {
    
    public void addFilteredData(Collection<DataType> data);

}
