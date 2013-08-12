package com.sap.sailing.datamining.impl.gpsfix;

import java.util.Collection;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.GroupByDimension;
import com.sap.sailing.datamining.shared.GenericGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class GroupGPSFixesByDimension<ValueType> extends GroupByDimension<GPSFixWithContext, ValueType> {

    public GroupGPSFixesByDimension(Collection<Dimension<GPSFixWithContext, ValueType>> dimensions) {
        super(dimensions);
    }

    @Override
    protected GroupKey createGroupKeyFor(GPSFixWithContext dataEntry, Dimension<GPSFixWithContext, ValueType> dimension) {
        return new GenericGroupKey<ValueType>(dimension.getDimensionValueFrom(dataEntry));
    }

}
