package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.util.impl.ArrayListNavigableSet;
import com.sap.sailing.util.impl.UnmodifiableNavigableSet;

/**
 * The only "raw" fix produced by this wind track implementation is based on the course layout of the tracked race to
 * which it is bound in the constructor. Such a fix will only be "emulated" if the
 * {@link TrackedRace#raceIsKnownToStartUpwind() race is known to start with an upwind leg}. Otherwise, this wind
 * track will remain empty. The track will also be empty if no {@link TrackedRace#getStart() start time} is known
 * for the tracked race.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class CourseBasedWindTrackImpl extends WindTrackImpl {
    private final TrackedRace trackedRace;
    private static final NavigableSet<Wind> empty = new UnmodifiableNavigableSet<Wind>(new ArrayListNavigableSet<Wind>(WindComparator.INSTANCE));
    
    public CourseBasedWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage) {
        super(millisecondsOverWhichToAverage);
        this.trackedRace = trackedRace;
    }

    @Override
    protected NavigableSet<Wind> getInternalRawFixes() {
        NavigableSet<Wind> result;
        if (trackedRace.raceIsKnownToStartUpwind()) {
            TimePoint startTime = trackedRace.getStart();
            if (startTime != null) {
                result = new ArrayListNavigableSet<Wind>(1, WindComparator.INSTANCE);
                result.add(trackedRace.getDirectionFromStartToNextMark(startTime));
            } else {
                result = empty;
            }
        } else {
            result = empty;
        }
        return result;
    }
    
    
}
