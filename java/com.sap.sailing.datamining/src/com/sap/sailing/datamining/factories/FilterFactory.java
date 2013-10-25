package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.GPSFixDimensionManager;
import com.sap.sailing.datamining.dimensions.TrackedLegOfCompetitorDimensionManager;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class FilterFactory {
    
    private static final DimensionManager<GPSFixWithContext> GPSFixDimensionManager = new GPSFixDimensionManager();
    private static final DimensionManager<TrackedLegOfCompetitorWithContext> TrackedLegOfCompetitorDimensionManager = new TrackedLegOfCompetitorDimensionManager();

    private FilterFactory() { }

    @SuppressWarnings("unchecked")
    public static <DataType> Filter<DataType> createDimensionFilter(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        if (selection.isEmpty()) {
            return createNoFilter();
        }
        return (Filter<DataType>) createCriteriaFilter(createAndCompoundDimensionFilterCritera(dataType, selection));
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

    private static <DataType> FilterCriteria<DataType> createAndCompoundDimensionFilterCritera(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        DimensionManager<DataType> dimensionManager = getDimensionManagerFor(dataType);
        CompoundFilterCriteria<DataType> compoundCriteria = new AndCompoundFilterCriteria<DataType>();

        for (Entry<SharedDimension, Iterable<?>> entry : selection.entrySet()) {
            Dimension<DataType, ?> dimension = dimensionManager.getDimensionFor(entry.getKey());
            if (dimension != null) {
                FilterCriteria<DataType> criteria = createDimensionFilterCriteria(dimension, entry.getValue());
                compoundCriteria.addCriteria(criteria);
            }
        }
        return compoundCriteria;
    }

    @SuppressWarnings("unchecked")
    private static <DataType> DimensionManager<DataType> getDimensionManagerFor(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (DimensionManager<DataType>) GPSFixDimensionManager;
        case TrackedLegOfCompetitor:
            return (DimensionManager<DataType>) TrackedLegOfCompetitorDimensionManager;
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    public static <DataType> Filter<DataType> createCriteriaFilter(FilterCriteria<DataType> criteria) {
        return new FilterByCriteria<DataType>(criteria);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

}
