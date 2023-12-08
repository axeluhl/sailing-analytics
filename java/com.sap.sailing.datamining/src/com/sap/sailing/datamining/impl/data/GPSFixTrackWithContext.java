package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasGPSFixTrackContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GPSFixTrackWithContext implements HasGPSFixTrackContext {

    private final HasRaceOfCompetitorContext raceOfCompetitorContext;
    
    private final GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack;
    
    public GPSFixTrackWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext, GPSFixTrack<Competitor, GPSFixMoving> gpsFixTrack) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.gpsFixTrack = gpsFixTrack;
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public GPSFixTrack<Competitor, GPSFixMoving> getGPSFixTrack() {
        return gpsFixTrack;
    }

    @Override
    public Duration getTimeSpentTackType() {
        if(getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace() != null) {
            final TimePoint endOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfRace();
            final TimePoint startOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getStartOfRace();
            final TimePoint end;
            if (endOfRace == null) {
                final TimePoint endOfTracking = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
                end = endOfTracking == null ? MillisecondsTimePoint.now() : endOfTracking;
            } else {
                end = endOfRace;
            }
            return gpsFixTrack.getTimeSpentTackType(startOfRace, end);
        }
        return null;
    }

    @Override
    public Distance getDistanceSpentTackType() {
        if(getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace() != null) {
            final TimePoint endOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfRace();
            final TimePoint startOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getStartOfRace();
            final TimePoint end;
            if (endOfRace == null) {
                final TimePoint endOfTracking = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
                end = endOfTracking == null ? MillisecondsTimePoint.now() : endOfTracking;
            } else {
                end = endOfRace;
            }
            return gpsFixTrack.getDistanceSpentTackType(
                    startOfRace,
                    end);
        }
        return null;
    }
}
