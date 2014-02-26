package com.sap.sailing.datamining;

public interface ConcurrentFilterCriteria<DataType> {
    
    public boolean matches(DataType dataEntry);

}
