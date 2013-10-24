package com.sap.sailing.datamining.impl.trackedLegOfCompetitor;

import java.util.Collection;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.GroupByDimension;

public class GroupTrackedLegOfCompetitorByDimension<ValueType> extends
        GroupByDimension<TrackedLegOfCompetitorWithContext, ValueType> {

    public GroupTrackedLegOfCompetitorByDimension(
            Collection<Dimension<TrackedLegOfCompetitorWithContext, ValueType>> dimensions) {
        super(dimensions);
    }

}
