package com.sap.sailing.datamining.impl.trackedLeg;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.datamining.data.TrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.datamining.impl.AbstractDataRetriever;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.server.RacingEventService;

public class TrackedLegRetriever extends AbstractDataRetriever<TrackedLegOfCompetitorWithContext> {

    @Override
    public Collection<TrackedLegOfCompetitorWithContext> retrieveData(RacingEventService racingEventService) {
        Collection<TrackedLegOfCompetitorWithContext> data = new ArrayList<TrackedLegOfCompetitorWithContext>();
        Collection<Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext>> baseData = retrieveDataTillTrackedLegOfCompetitor(racingEventService);
        for (Pair<TrackedLegOfCompetitor, TrackedLegOfCompetitorContext> baseDataEntry : baseData) {
            data.add(new TrackedLegOfCompetitorWithContextImpl(baseDataEntry.getA(), baseDataEntry.getB()));
        }
        return data;
    }

}
