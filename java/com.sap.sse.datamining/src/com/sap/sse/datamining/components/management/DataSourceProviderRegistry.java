package com.sap.sse.datamining.components.management;

import com.sap.sse.datamining.DataSourceProvider;

public interface DataSourceProviderRegistry extends DataSourceProviderProvider {

    boolean register(DataSourceProvider<?> dataSourceProvider);
    boolean unregister(DataSourceProvider<?> dataSourceProvider);

}
