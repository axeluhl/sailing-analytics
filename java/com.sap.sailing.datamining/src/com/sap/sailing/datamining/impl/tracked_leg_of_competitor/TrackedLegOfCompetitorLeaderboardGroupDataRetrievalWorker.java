package com.sap.sailing.datamining.impl.tracked_leg_of_competitor;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractLeaderboardGroupDataRetrievalWorker;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TrackedLegOfCompetitorLeaderboardGroupDataRetrievalWorker extends AbstractLeaderboardGroupDataRetrievalWorker<TrackedLegOfCompetitorWithContext> {

    @Override
    public Collection<TrackedLegOfCompetitorWithContext> doWork() {
        Collection<TrackedLegOfCompetitorWithContext> data = new ArrayList<TrackedLegOfCompetitorWithContext>();
        Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> baseData = retrieveDataTillTrackedLegOfCompetitor(getGroup());
        for (Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext> baseDataEntry : baseData) {
            data.add(new TrackedLegOfCompetitorWithContextImpl(baseDataEntry.getA(), baseDataEntry.getB()));
        }
        return data;
    }

}
