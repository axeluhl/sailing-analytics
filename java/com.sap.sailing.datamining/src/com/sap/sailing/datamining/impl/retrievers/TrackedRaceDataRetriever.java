package com.sap.sailing.datamining.impl.retrievers;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.DataRetriever;
import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.impl.GPSFixContextImpl;
import com.sap.sailing.datamining.impl.GPSFixWithContextImpl;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceDataRetriever implements DataRetriever {

    private TrackedRace trackedRace;

    public TrackedRaceDataRetriever(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public List<GPSFixWithContext> retrieveData() {
        List<GPSFixWithContext> data = new ArrayList<GPSFixWithContext>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
            track.lockForRead();
            try {
                for (GPSFixMoving gpsFix : track.getFixes()) {
                    GPSFixContext context = new GPSFixContextImpl(trackedRace, competitor);
                    data.add(new GPSFixWithContextImpl(gpsFix, context));
                }
            } finally {
                track.unlockAfterRead();
            }
        }
        return data;
    }

}
