package com.sap.sse.datamining;

import com.sap.sse.datamining.components.AggregationProcessorDefinition;
import com.sap.sse.datamining.components.DataRetrieverChainDefinition;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;
import com.sap.sse.i18n.ResourceBundleStringMessages;

/**
 * A Service that provides all necessary elements of a datamining bundle. A datamining bundle
 * has to register such a service to the context. Such registrations are automatically tracked
 * by the domain independent datamining bundle, which calls the methods an integrates the bundle
 * in the datamining framework.
 * 
 * @author Lennart Hensler (D054527)
 *
 */
public interface DataMiningBundleService {
    
    /**
     * @return a {@link ResourceBundleStringMessages} that can provide the string messages used by this bundle.
     */
    public ResourceBundleStringMessages getStringMessages();

    /**
     * This method provides a collection of Classes, that contain methods that are annotated with the datamining
     * annotations (e.g. {@link Dimension} or {@link Statistic}). These classes will be scanned and used to build
     * the dimensions and statistics for this bundle.
     * 
     * @return a collection of Classes, that contain methods that are annotated with the datamining annotations
     */
    public Iterable<Class<?>> getClassesWithMarkedMethods();
    
    /**
     * @return a collection of {@link DataRetrieverChainDefinition DataRetrieverChainDefinitions}, that are used by this
     *         bundle to retrieve the data
     */
    public Iterable<DataRetrieverChainDefinition<?, ?>> getDataRetrieverChainDefinitions();

    /**
     * @return a collection of {@link DataSourceProvider DataSourceProviders}, that are used to get the data sources for the queries
     */
    public Iterable<DataSourceProvider<?>> getDataSourceProviders();
    
    /**
     * @return a collection of {@link AggregationProcessorDefinition AggregationProcessorDefinitions}, that are used by this
     *         bundle to aggregate the result.
     */
    public Iterable<AggregationProcessorDefinition<?, ?>> getAggregationProcessorDefinitions();

}
