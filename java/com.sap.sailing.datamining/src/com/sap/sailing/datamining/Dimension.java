package com.sap.sailing.datamining;


public interface Dimension<DataType> {
    
    public String getName();

    public String getDimensionValueFrom(DataType data);

}
