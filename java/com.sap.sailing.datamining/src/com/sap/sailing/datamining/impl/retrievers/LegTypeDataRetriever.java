package com.sap.sailing.datamining.impl.retrievers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LegTypeDataRetriever extends AbstractTrackedRaceDataRetriever {

    private HashSet<LegType> legTypes;

    public LegTypeDataRetriever(TrackedRace trackedRace, Collection<LegType> legTypes) {
        super(trackedRace);
        this.legTypes = new HashSet<LegType>(legTypes);
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Leg leg : getTrackedRace().getRace().getCourse().getLegs()) {
            for (LegType legType : legTypes) {
                try {
                    if (legType.equals(getLegType(leg))) {
                        data.addAll(legToGPSFixesWithContext(leg));
                    }
                } catch (NoWindException e) { }
            }
        }
        return data;
    }

    private LegType getLegType(Leg leg) throws NoWindException {
        TimePoint at = null;
        TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
        for (TrackedLegOfCompetitor trackedLegOfCompetitor : trackedLeg.getTrackedLegsOfCompetitors()) {
            TimePoint start = trackedLegOfCompetitor.getStartTime();
            TimePoint finish = trackedLegOfCompetitor.getFinishTime();
            if (start != null && finish != null) {
                at = new MillisecondsTimePoint((start.asMillis() + finish.asMillis()) / 2);
                break;
            }
        }
        return trackedLeg.getLegType(at);
    }

}
