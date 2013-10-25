package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.dimensions.GPSFixDimensionsManager;
import com.sap.sailing.datamining.dimensions.TrackedLegOfCompetitorDimensionsManager;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class FilterFactory {
    
    private FilterFactory() { }

    @SuppressWarnings("unchecked")
    public static <DataType> Filter<DataType> createDimensionFilter(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        if (selection.isEmpty()) {
            return createNoFilter();
        }
        
        switch (dataType) {
        case GPSFix:
            return (Filter<DataType>) createCriteriaFilter(createGPSFixDimensionFilterCriteria(selection));
        case TrackedLegOfCompetitor:
            return (Filter<DataType>) createCriteriaFilter(createTrackedLegOfCompetitorDimensionFilterCriteria(selection));
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
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
    
    private static FilterCriteria<TrackedLegOfCompetitorWithContext> createTrackedLegOfCompetitorDimensionFilterCriteria(Map<SharedDimension, Iterable<?>> selection) {
        CompoundFilterCriteria<TrackedLegOfCompetitorWithContext> compoundFilterCriteria = new AndCompoundFilterCriteria<TrackedLegOfCompetitorWithContext>();
        for (Entry<SharedDimension, Iterable<?>> selectionEntry : selection.entrySet()) {
            FilterCriteria<TrackedLegOfCompetitorWithContext> criteria = createTrackedLegOfCompetitorDimensionFilterCriteria(selectionEntry.getKey(), selectionEntry.getValue());
            compoundFilterCriteria.addCriteria(criteria);
        }
        return compoundFilterCriteria;
    }

    private static <ValueType> FilterCriteria<TrackedLegOfCompetitorWithContext> createTrackedLegOfCompetitorDimensionFilterCriteria(SharedDimension sharedDimension, Iterable<?> values) {
        Dimension<TrackedLegOfCompetitorWithContext, ValueType> dimension = TrackedLegOfCompetitorDimensionsManager.getDimensionFor(sharedDimension);
        return createDimensionFilterCriteria(dimension, values);
    }

    public static FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(Map<SharedDimension, Iterable<?>> selection) {
        CompoundFilterCriteria<GPSFixWithContext> compoundFilterCriteria = new AndCompoundFilterCriteria<GPSFixWithContext>();
        for (Entry<SharedDimension, Iterable<?>> selectionEntry : selection.entrySet()) {
            FilterCriteria<GPSFixWithContext> criteria = createGPSFixDimensionFilterCriteria(selectionEntry.getKey(), selectionEntry.getValue());
            compoundFilterCriteria.addCriteria(criteria);
        }
        return compoundFilterCriteria;
    }
    
    public static <ValueType> FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(SharedDimension sharedDimension, Iterable<?> values) {
        Dimension<GPSFixWithContext, ValueType> dimension = GPSFixDimensionsManager.getDimensionFor(sharedDimension);
        return createDimensionFilterCriteria(dimension, values);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

}
