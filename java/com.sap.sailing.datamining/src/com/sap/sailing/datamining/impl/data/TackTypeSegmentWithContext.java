package com.sap.sailing.datamining.impl.data;

import java.text.SimpleDateFormat;

import com.sap.sailing.datamining.data.HasGPSFixTrackContext;
import com.sap.sailing.datamining.data.HasTackTypeSegmentContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class TackTypeSegmentWithContext implements HasTackTypeSegmentContext {
    private final HasGPSFixTrackContext gpsFixTrackContext;
    private final TimePoint startOfTackTypeSegment;
    private final TimePoint endOfTackTypeSegment;
    private static final SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    
    public TackTypeSegmentWithContext(GPSFixTrackWithContext gpsFixTrackWithContext, TimePoint startOfTackTypeSegment,
            TimePoint endOfTackTypeSegment) {
        super();
        this.gpsFixTrackContext = gpsFixTrackWithContext;
        this.startOfTackTypeSegment = startOfTackTypeSegment;
        this.endOfTackTypeSegment = endOfTackTypeSegment;
    }

    @Override
    public String getName() {
        return gpsFixTrackContext.getRaceOfCompetitorContext().getCompetitor().getName() + "@"
                + TIMEPOINT_FORMATTER.format(startOfTackTypeSegment.asDate());
    }

    @Override
    public HasGPSFixTrackContext getGPSFixTrackContext() {
        return gpsFixTrackContext;
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
        return getGPSFixTrackContext().getRaceOfCompetitorContext().getCompetitor();
    }

    private TrackedRace getTrackedRace() {
        return getGPSFixTrackContext().getRaceOfCompetitorContext().getTrackedRaceContext().getTrackedRace();
    }
}
