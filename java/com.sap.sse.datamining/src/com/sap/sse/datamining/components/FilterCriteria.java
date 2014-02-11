package com.sap.sse.datamining.components;

public interface FilterCriteria<DataType> {
    
    public boolean matches(DataType dataEntry);

}
