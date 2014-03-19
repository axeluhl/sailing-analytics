package com.sap.sse.datamining.data.deprecated;

public interface Dimension<DataType, ValueType> {
    
    public String getName();

    public ValueType getDimensionValueFrom(DataType dataEntry);

}
