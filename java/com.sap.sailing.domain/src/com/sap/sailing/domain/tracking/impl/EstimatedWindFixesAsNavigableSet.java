package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindTrack;

/**
 * Emulates a collection of {@link Wind} fixes for a {@link TrackedRace}, computed using
 * {@link TrackedRace#getEstimatedWindDirection(com.sap.sailing.domain.base.Position, TimePoint)}. If not constrained
 * by a {@link #from} and/or a {@link #to} time point, an equidistant time field is assumed, starting at
 * {@link TrackedRace#getStart()} and leading up to {@link TrackedRace#getTimePointOfNewestEvent()}. If
 * {@link TrackedRace#getStart()} returns <code>null</code>, {@link Long#MAX_VALUE} is used as the {@link #from}
 * time point, pushing the start to the more or less infinite future ("end of the universe"). If no event was
 * received yet and hence {@link TrackedRace#getTimePointOfNewestEvent()} returns <code>null</code>, the {@link #to}
 * end is assumed to be the beginning of the epoch (1970-01-01T00:00:00).
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class EstimatedWindFixesAsNavigableSet extends VirtualWindFixesAsNavigableSet {
    public EstimatedWindFixesAsNavigableSet(TrackBasedEstimationWindTrackImpl track, TrackedRace trackedRace) {
        this(track, trackedRace, null, null);
    }
    
    /**
     * @param from expected to be an integer multiple of {@link #getResolutionInMilliseconds()} or <code>null</code>
     * @param to expected to be an integer multiple of {@link #getResolutionInMilliseconds()} or <code>null</code>
     */
    private EstimatedWindFixesAsNavigableSet(WindTrack track, TrackedRace trackedRace,
            TimePoint from, TimePoint to) {
        super(track, trackedRace, from, to, /* resolution in milliseconds */ 1000l);
    }

    protected Wind getWind(Position p, TimePoint timePoint) {
        return getTrackedRace().getEstimatedWindDirection(p, timePoint);
    }

    @Override
    protected NavigableSet<Wind> createSubset(WindTrack track, TrackedRace trackedRace, TimePoint from, TimePoint to) {
        return new EstimatedWindFixesAsNavigableSet(track, trackedRace, from, to);
    }

}

