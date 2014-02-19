package com.sap.sse.datamining.data;

public interface Dimension<DataType, ValueType> {
    
    public String getName();

    public ValueType getDimensionValueFrom(DataType dataEntry);

}
