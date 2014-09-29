package com.sap.sse.datamining;


public interface DataRetrieverChainDefinition<DataSourceType> {
    
    public Class<? super DataSourceType> getDataSourceType();
    
    public boolean canRetrieve(Class<?> dataType);

    public DataRetrieverChainBuilder<DataSourceType> startBuilding();

}
