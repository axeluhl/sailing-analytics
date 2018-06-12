package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasBravoFixTrackContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.BravoFixTrack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class BravoFixTrackWithContext implements HasBravoFixTrackContext {

    private final HasRaceOfCompetitorContext raceOfCompetitorContext;
    
    private final BravoFixTrack<Competitor> bravoFixTrack;
    
    public BravoFixTrackWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext, BravoFixTrack<Competitor> bravoFixTrack) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.bravoFixTrack = bravoFixTrack;
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public BravoFixTrack<Competitor> getBravoFixTrack() {
        return bravoFixTrack;
    }

    @Override
    public Duration getTimeSpentFoiling() {
        final TimePoint endOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfRace();
        final TimePoint startOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getStartOfRace();
        final TimePoint end;
        if (endOfRace == null) {
            final TimePoint endOfTracking = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
            end = endOfTracking == null ? MillisecondsTimePoint.now() : endOfTracking;
        } else {
            end = endOfRace;
        }
        return bravoFixTrack.getTimeSpentFoiling(startOfRace, end);
    }

    @Override
    public Distance getDistanceSpentFoiling() {
        final TimePoint endOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfRace();
        final TimePoint startOfRace = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getStartOfRace();
        final TimePoint end;
        if (endOfRace == null) {
            final TimePoint endOfTracking = getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace().getEndOfTracking();
            end = endOfTracking == null ? MillisecondsTimePoint.now() : endOfTracking;
        } else {
            end = endOfRace;
        }
        return bravoFixTrack.getDistanceSpentFoiling(
                startOfRace,
                end);
    }
}
