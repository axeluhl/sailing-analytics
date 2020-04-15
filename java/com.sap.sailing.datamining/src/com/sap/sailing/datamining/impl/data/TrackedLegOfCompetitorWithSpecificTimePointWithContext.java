package com.sap.sailing.datamining.impl.data;

import java.util.function.BiFunction;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.common.TimePoint;

/**
 * While a {@link TrackedLegOfCompetitorWithContext} considers as the time point for various calculations such as for
 * {@link #getTack()} the middle of the leg, or for time ranges the entire duration of the leg, this subtype focuses the
 * calculations to a specific time point that is assumed to be within the leg, such as the time point of a single
 * specific GPS fix or a maneuver. Values calculated for the leg's entire time range will instead be calculated from the
 * leg's start to the time point specified for this object here.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class TrackedLegOfCompetitorWithSpecificTimePointWithContext extends TrackedLegOfCompetitorWithContext {
    private static final long serialVersionUID = -6512942220185977262L;
    private final TimePoint timePoint;

    public TrackedLegOfCompetitorWithSpecificTimePointWithContext(HasTrackedLegContext trackedLegContext,
            TrackedLegOfCompetitor trackedLegOfCompetitor, TimePoint timePoint) {
        super(trackedLegContext, trackedLegOfCompetitor);
        this.timePoint = timePoint;
    }

    @Override
    protected <R> R getSomethingForLegTrackingInterval(BiFunction<TimePoint, TimePoint, R> resultSupplier) {
        final TimePoint startTime = getTrackedLegOfCompetitor().getStartTime();
        final TimePoint finishTime = getTimePoint();
        return getSomethingForInterval(resultSupplier, startTime, finishTime);
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
    }
}
