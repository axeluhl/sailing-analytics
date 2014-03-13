package com.sap.sailing.datamining;


public interface Dimension<DataType, ValueType> {
    
    public String getName();

    public ValueType getDimensionValueFrom(DataType dataEntry);

}
