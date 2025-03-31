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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataSourceType == null) ? 0 : dataSourceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractDataSourceProvider<?> other = (AbstractDataSourceProvider<?>) obj;
        if (dataSourceType == null) {
            if (other.dataSourceType != null)
                return false;
        } else if (!dataSourceType.equals(other.dataSourceType))
            return false;
        return true;
    }

}
