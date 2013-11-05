package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.Grouper;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.impl.DynamicGrouper;
import com.sap.sailing.datamining.impl.GroupByDimension;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixBaseBindingProvider;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class GrouperFactory {
    
    private GrouperFactory() { }

    public static <DataType> Grouper<DataType> createGrouper(QueryDefinition queryDefinition) {
        switch (queryDefinition.getGrouperType()) {
        case Custom:
            return createDynamicGrouper(queryDefinition.getCustomGrouperScriptText(), queryDefinition.getDataType());
        case Dimensions:
            return createByDimensionGrouper(queryDefinition.getDataType(), queryDefinition.getDimensionsToGroupBy());
        }
        throw new IllegalArgumentException("Not yet implemented for the given grouper type: "
                + queryDefinition.getGrouperType().toString());
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
    private static <DataType, ValueType> Grouper<DataType> createByDimensionGrouper(DataTypes dataType, List<SharedDimension> dimensionsToGroupBy) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        Collection<Dimension<DataType, ValueType>> dimensions = new LinkedHashSet<Dimension<DataType, ValueType>>();
        for (SharedDimension sharedDimension : dimensionsToGroupBy) {
            Dimension<DataType, ValueType> dimension = (Dimension<DataType, ValueType>) dimensionManager.getDimensionFor(sharedDimension);
            dimensions.add(dimension);
        }
        return new GroupByDimension<DataType, ValueType>(dimensions);
    }

}
