package com.sap.sailing.datamining.impl.data;

import java.text.SimpleDateFormat;

import com.sap.sailing.datamining.data.HasBravoFixTrackContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class TackTypeSegmentWithContext implements HasTackTypeSegmentContext {
    private final HasBravoFixTrackContext bravoFixTrackContext;
    private final TimePoint startOfTackTypeSegment;
    private final TimePoint endOfTackTypeSegment;
    private static final SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    public TackTypeSegmentWithContext(HasBravoFixTrackContext bravoFixTrackContext, TimePoint startOfTackTypeSegment,
            TimePoint endOfTackTypeSegment) {
        super();
        this.bravoFixTrackContext = bravoFixTrackContext;
        this.startOfTackTypeSegment = startOfTackTypeSegment;
        this.endOfTackTypeSegment = endOfTackTypeSegment;
    }

    @Override
    public String getName() {
        return bravoFixTrackContext.getRaceOfCompetitorContext().getCompetitor().getName() + "@"
                + TIMEPOINT_FORMATTER.format(startOfTackTypeSegment.asDate());
    }

    @Override
    public HasBravoFixTrackContext getBravoFixTrackContext() {
        return bravoFixTrackContext;
    }

    @Override
    public TimePoint getStartOfTackTypeSegment() {
        return startOfTackTypeSegment;
    }

    @Override
    public TimePoint getEndOfTackTypeSegment() {
        return endOfTackTypeSegment;
    }

    @Override
    public Duration getDuration() {
        return getStartOfTackTypeSegment().until(getEndOfTackTypeSegment());
    }

    @Override
    public Distance getDistance() {
        return getGpsFixTrack().
                getDistanceTraveled(getStartOfTackTypeSegment(), getEndOfTackTypeSegment());
    }

    private GPSFixTrack<Competitor, GPSFixMoving> getGpsFixTrack() {
        return getTrackedRace().getTrack(getCompetitor());
    }

    private Competitor getCompetitor() {
        return getBravoFixTrackContext().getRaceOfCompetitorContext().getCompetitor();
    }

    private TrackedRace getTrackedRace() {
        return getBravoFixTrackContext().getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace();
    }
}
