package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.datamining.dimensions.GPSFixDimensionsManager;
import com.sap.sailing.datamining.impl.DynamicGrouper;
import com.sap.sailing.datamining.impl.SmartQueryDefinition;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixBaseBindingProvider;
import com.sap.sailing.datamining.impl.gpsfix.GroupGPSFixesByDimension;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class GrouperFactory {
    
    private GrouperFactory() { }

    public static <DataType> Grouper<DataType> createGrouper(SmartQueryDefinition smartQueryDefinition) {
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
            Dimension<GPSFixWithContext, ValueType> dimension = GPSFixDimensionsManager.getDimensionFor(dimensionType);
            dimensions.add(dimension);
        }
        return new GroupGPSFixesByDimension<ValueType>(dimensions);
    }

}
