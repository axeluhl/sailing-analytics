package com.sap.sse.datamining.impl;

import com.sap.sse.datamining.DataSourceProvider;

public abstract class AbstractDataSourceProvider<DataSourceType> implements DataSourceProvider<DataSourceType> {
    
    private final Class<DataSourceType> dataSourceType;

    public AbstractDataSourceProvider(Class<DataSourceType> dataSourceType) {
        this.dataSourceType = dataSourceType;
    }
    
    @Override
    public Class<DataSourceType> getDataSourceType() {
        return dataSourceType;
    }

}
