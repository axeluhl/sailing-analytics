package com.sap.sailing.datamining.impl.gpsfix;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.GroupByDimension;
import com.sap.sailing.datamining.impl.StringGroupKey;
import com.sap.sailing.datamining.shared.GroupKey;

public class GroupGPSFixesByDimension extends GroupByDimension<GPSFixWithContext> {

    public GroupGPSFixesByDimension(Dimension<GPSFixWithContext>... dimensions) {
        super(dimensions);
    }

    @Override
    protected GroupKey createGroupKeyFor(GPSFixWithContext dataEntry, Dimension<GPSFixWithContext> dimension) {
        return new StringGroupKey(dimension.getDimensionValueFrom(dataEntry));
    }

}
