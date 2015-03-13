package com.sap.sse.datamining;

public interface DataSourceProvider<DataSourceType> {
    
    public Class<DataSourceType> getDataSourceType();
    
    public DataSourceType getDataSource();

}
