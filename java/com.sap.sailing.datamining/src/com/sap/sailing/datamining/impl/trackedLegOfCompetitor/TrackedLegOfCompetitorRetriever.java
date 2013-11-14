package com.sap.sailing.datamining.impl.trackedLegOfCompetitor;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.SingleThreadedDataRetriever;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractLeaderboardGroupDataRetriever;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TrackedLegOfCompetitorRetriever extends AbstractLeaderboardGroupDataRetriever<TrackedLegOfCompetitorWithContext> {

    @Override
    public Collection<TrackedLegOfCompetitorWithContext> retrieveData() {
        Collection<TrackedLegOfCompetitorWithContext> data = new ArrayList<TrackedLegOfCompetitorWithContext>();
        Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> baseData = retrieveDataTillTrackedLegOfCompetitor(getGroup());
        for (Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext> baseDataEntry : baseData) {
            data.add(new TrackedLegOfCompetitorWithContextImpl(baseDataEntry.getA(), baseDataEntry.getB()));
        }
        return data;
    }

    @Override
    public SingleThreadedDataRetriever<TrackedLegOfCompetitorWithContext> clone() {
        return new TrackedLegOfCompetitorRetriever();
    }

}
