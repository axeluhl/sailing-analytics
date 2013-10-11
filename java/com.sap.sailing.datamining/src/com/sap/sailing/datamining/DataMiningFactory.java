package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.impl.DataAmountExtractor;
import com.sap.sailing.datamining.impl.DynamicGrouper;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.SmartQueryDefinition;
import com.sap.sailing.datamining.impl.SpeedInKnotsExtractor;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleArithmeticAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleDoubleSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerArithmeticAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.SimpleIntegerSumAggregator;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixBaseBindingProvider;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetriever;
import com.sap.sailing.datamining.impl.gpsfix.GroupGPSFixesByDimension;
import com.sap.sailing.datamining.shared.Components.AggregatorType;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.domain.base.Moving;

public class DataMiningFactory {
    
    private DataMiningFactory() { }

    public static <DataType, ExtractedType, AggregatedType> Query<DataType, AggregatedType> createQuery(
            DataRetriever<DataType> retriever, Filter<DataType> filter, Grouper<DataType> grouper,
            Extractor<DataType, ExtractedType> extractor, Aggregator<ExtractedType, AggregatedType> aggregator) {
        return new QueryImpl<DataType, ExtractedType, AggregatedType>(retriever, filter, grouper, extractor, aggregator);
    }

    public static <DataType, AggregatedType extends Number> Query<DataType, AggregatedType> createQuery(QueryDefinition queryDefinition) {
        SmartQueryDefinition smartQueryDefinition = new SmartQueryDefinition(queryDefinition);
        
        DataRetriever<DataType> retriever = createDataRetriever(smartQueryDefinition.getDataType());
        Filter<DataType> filter = createDimensionFilter(smartQueryDefinition.getDataType(), smartQueryDefinition.getSelection());
        Grouper<DataType> grouper = createGrouper(smartQueryDefinition);
        Extractor<DataType, AggregatedType> extractor = createExtractor(smartQueryDefinition.getStatisticType());
        Aggregator<AggregatedType, AggregatedType> aggregator = createAggregator(smartQueryDefinition.getStatisticType(), smartQueryDefinition.getAggregatorType());
        return new QueryImpl<DataType, AggregatedType, AggregatedType>(retriever, filter, grouper, extractor, aggregator);
    }

    /**
     * Creates a retriever for the given data type. Throws an exception, if the used <code>DataType</code> doesn't match the <code>DataType</code> of the returning retriever.
     */
    @SuppressWarnings("unchecked")
    public static <DataType> DataRetriever<DataType> createDataRetriever(com.sap.sailing.datamining.shared.DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (DataRetriever<DataType>) createGPSFixRetriever();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    public static DataRetriever<GPSFixWithContext> createGPSFixRetriever() {
        return new GPSFixRetriever();
    }
    
    /**
     * @return A filter that filters nothing. So the returning collection is the same as the given one.
     */
    public static <DataType> Filter<DataType> createNoFilter() {
        return new Filter<DataType>() {
            @Override
            public Collection<DataType> filter(Collection<DataType> data) {
                return data;
            }
        };
    }

    public static <DataType> Filter<DataType> createCriteriaFilter(FilterCriteria<DataType> criteria) {
        return new FilterByCriteria<DataType>(criteria);
    }

    @SuppressWarnings("unchecked")
    private static <DataType> Filter<DataType> createDimensionFilter(DataTypes dataType, Map<?, Iterable<?>> selection) {
        switch (dataType) {
        case GPSFix:
            return (Filter<DataType>) createGPSFixDimensionFilter((Map<SharedDimension, Iterable<?>>) selection);
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }
    
    public static Filter<GPSFixWithContext> createGPSFixDimensionFilter(Map<SharedDimension, Iterable<?>> selection) {
        if (selection.isEmpty()) {
            return createNoFilter();
        }
        
        return createCriteriaFilter(createGPSFixDimensionFilterCriteria(selection));
    }

    public static FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(Map<SharedDimension, Iterable<?>> selection) {
        CompoundFilterCriteria<GPSFixWithContext> compoundFilterCriteria = new AndCompoundFilterCriteria<GPSFixWithContext>();
        for (Entry<SharedDimension, Iterable<?>> selectionEntry : selection.entrySet()) {
            FilterCriteria<GPSFixWithContext> criteria = createGPSFixDimensionFilterCriteria(selectionEntry.getKey(), selectionEntry.getValue());
            compoundFilterCriteria.addCriteria(criteria);
        }
        return compoundFilterCriteria;
    }
    
    public static <ValueType> FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(SharedDimension gpsFix, Iterable<?> values) {
        Dimension<GPSFixWithContext, ValueType> dimension = Dimensions.GPSFix.getDimensionFor(gpsFix);
        return createDimensionFilterCriteria(dimension, values);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

    private static <DataType> Grouper<DataType> createGrouper(SmartQueryDefinition smartQueryDefinition) {
        switch (smartQueryDefinition.getGrouperType()) {
        case Custom:
            return createDynamicGrouper(smartQueryDefinition.getCustomGrouperScriptText(), smartQueryDefinition.getDataType());
        case Dimensions:
            return createByDimensionGrouper(smartQueryDefinition.getDataType(), smartQueryDefinition.getDimensionsToGroupBy());
        }
        throw new IllegalArgumentException("Not yet implemented for the given grouper type: "
                + smartQueryDefinition.getGrouperType().toString());
    }

    public static <DataType> Grouper<DataType> createDynamicGrouper(String grouperScriptText, DataTypes dataType) {
        BaseBindingProvider<DataType> baseBindingProvider = createBaseBindingProvider(dataType);
        return new DynamicGrouper<DataType>(grouperScriptText, baseBindingProvider);
    }

    @SuppressWarnings("unchecked")
    private static <DataType> BaseBindingProvider<DataType> createBaseBindingProvider(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (BaseBindingProvider<DataType>) new GPSFixBaseBindingProvider();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    @SuppressWarnings("unchecked")
    private static <DataType> Grouper<DataType> createByDimensionGrouper(DataTypes dataType, List<?> dimensionsToGroupBy) {
        switch (dataType) {
        case GPSFix:
            return (Grouper<DataType>) createGPSFixByDimensionGrouper((Collection<SharedDimension>) dimensionsToGroupBy);
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    public static <ValueType> Grouper<GPSFixWithContext> createGPSFixByDimensionGrouper(Collection<SharedDimension> dimensionsToGroupBy) {
        Collection<Dimension<GPSFixWithContext, ValueType>> dimensions = new LinkedHashSet<Dimension<GPSFixWithContext, ValueType>>();
        for (SharedDimension dimensionType : dimensionsToGroupBy) {
            Dimension<GPSFixWithContext, ValueType> dimension = Dimensions.GPSFix.getDimensionFor(dimensionType);
            dimensions.add(dimension);
        }
        return new GroupGPSFixesByDimension<ValueType>(dimensions);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ExtractedType extends Number> Extractor<DataType, ExtractedType> createExtractor(StatisticType statisticType) {
        switch (statisticType) {
        case DataAmount:
            return (Extractor<DataType, ExtractedType>) createDataAmountExtractor();
        case Speed:
            return (Extractor<DataType, ExtractedType>) createSpeedExtractor();
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistic type: "
                + statisticType.toString());
    }
    
    public static Extractor<Moving, Double> createSpeedExtractor() {
        return new SpeedInKnotsExtractor();
    }

    public static <DataType> Extractor<DataType, Integer> createDataAmountExtractor() {
        return new DataAmountExtractor<DataType>();
    }

    @SuppressWarnings("unchecked")
    public static <ExtractedType, AggregatedType> Aggregator<ExtractedType, AggregatedType> createAggregator(StatisticType statisticToCalculate,
            AggregatorType aggregatorType) {
        switch (statisticToCalculate.getValueType()) {
        case Double:
            return (Aggregator<ExtractedType, AggregatedType>) createDoubleAggregator(aggregatorType);
        case Integer:
            return (Aggregator<ExtractedType, AggregatedType>) createIntegerAggregator(aggregatorType);
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistics value type: "
                + statisticToCalculate.getValueType().toString());
    }
    
    public static Aggregator<Integer, Integer> createIntegerAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleIntegerArithmeticAverageAggregator();
        case Sum:
            return new SimpleIntegerSumAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
    
    public static Aggregator<Double, Double> createDoubleAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new SimpleDoubleArithmeticAverageAggregator();
        case Sum:
            return new SimpleDoubleSumAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }

}
