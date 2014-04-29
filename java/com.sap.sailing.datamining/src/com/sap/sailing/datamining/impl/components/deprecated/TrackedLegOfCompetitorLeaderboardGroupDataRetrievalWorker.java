package com.sap.sailing.datamining.impl.components.deprecated;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.tracked_leg_of_competitor.TrackedLegOfCompetitorWithContextImpl;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TrackedLegOfCompetitorLeaderboardGroupDataRetrievalWorker extends AbstractLeaderboardGroupDataRetrievalWorker<TrackedLegOfCompetitorWithContext> {

    @Override
    public Collection<TrackedLegOfCompetitorWithContext> doWork() {
        Collection<TrackedLegOfCompetitorWithContext> data = new ArrayList<TrackedLegOfCompetitorWithContext>();
        Collection<Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext>> baseData = retrieveDataTillTrackedLegOfCompetitor(getGroup());
        for (Pair<TrackedLegOfCompetitor, HasTrackedLegOfCompetitorContext> baseDataEntry : baseData) {
            data.add(new TrackedLegOfCompetitorWithContextImpl(baseDataEntry.getA(), baseDataEntry.getB()));
        }
        return data;
    }

}
