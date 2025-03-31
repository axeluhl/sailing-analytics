package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;

public class CompetitorBravoFixTrackImpl extends BravoFixTrackImpl<Competitor> {
    private static final long serialVersionUID = 569731925060211875L;

    public CompetitorBravoFixTrackImpl(Competitor trackedItem, String trackName, boolean hasExtendedFixes,
            GPSFixTrack<Competitor, GPSFixMoving> gpsTrack) {
        super(trackedItem, trackName, hasExtendedFixes, gpsTrack);
    }

    public CompetitorBravoFixTrackImpl(Competitor trackedItem, String trackName, boolean hasExtendedFixes) {
        super(trackedItem, trackName, hasExtendedFixes);
    }
    
    @Override
    public void addedToTrackedRace(TrackedRace trackedRace) {
        if (this.getGpsTrack() == null) {
            this.setGpsTrack(trackedRace.getTrack(getTrackedItem()));
        }
    }
}
