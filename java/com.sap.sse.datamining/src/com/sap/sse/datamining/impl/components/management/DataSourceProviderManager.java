package com.sap.sse.datamining.impl.components.management;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.datamining.DataSourceProvider;
import com.sap.sse.datamining.components.management.DataSourceProviderRegistry;

public class DataSourceProviderManager implements DataSourceProviderRegistry {
    
    private static final Logger logger = Logger.getLogger(DataSourceProviderManager.class.getName());

    private final Map<Class<?>, DataSourceProvider<?>> sourceProvidersMappedBySourceType;
    
    public DataSourceProviderManager() {
        sourceProvidersMappedBySourceType = new HashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <DataSourceType> DataSourceProvider<DataSourceType> get(Class<DataSourceType> dataSourceType) {
        return (DataSourceProvider<DataSourceType>) sourceProvidersMappedBySourceType.get(dataSourceType);
    }

    @Override
    public boolean register(DataSourceProvider<?> dataSourceProvider) {
        Class<?> dataSourceType = dataSourceProvider.getDataSourceType();
        DataSourceProvider<?> previousProvider = sourceProvidersMappedBySourceType.put(dataSourceType, dataSourceProvider);
        if (previousProvider == null) {
            logger.info("Registering data source provider " + dataSourceProvider + " for data source type " + dataSourceType.getName());
        } else {
            logger.warning("Replacing data source provider " + previousProvider + " with " + dataSourceProvider + " for data source type " + dataSourceType.getName());
        }
        return true;
    }

    @Override
    public boolean unregister(DataSourceProvider<?> dataSourceProvider) {
        boolean changed = false;
        Class<?> dataSourceType = dataSourceProvider.getDataSourceType();
        DataSourceProvider<?> currentProvider = sourceProvidersMappedBySourceType.get(dataSourceType);
        if (currentProvider == null) {
            logger.info("Can't unregister " + dataSourceProvider + " because there is no provider registered for data source type " + dataSourceType);
        } else if (!currentProvider.equals(dataSourceProvider)) {
            logger.info("Can't unregister " + dataSourceProvider + " because theres the different provider " + currentProvider +
                    " registered for the data source type " + dataSourceType);
        } else {
            logger.info("Unregistering data source provider " + dataSourceProvider + " for data source type " + dataSourceType.getName());
            sourceProvidersMappedBySourceType.remove(dataSourceType);
            changed = true;
        }
        return changed;
    }

}
