package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.DataSourceProvider;

public interface DataSourceProviderProvider {
    
    public <DataSourceType> DataSourceProvider<DataSourceType> get(Class<DataSourceType> dataSourceType);

}
