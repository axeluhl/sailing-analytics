package com.sap.sailing.datamining;

import java.util.Collection;

public interface Filter<DataType> {
    
    public Collection<DataType> filter(Collection<DataType> data);

}