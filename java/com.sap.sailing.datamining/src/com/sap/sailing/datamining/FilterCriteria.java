package com.sap.sailing.datamining;

public interface FilterCriteria<DataType> {
    
    public boolean matches(DataType dataEntry);

}
