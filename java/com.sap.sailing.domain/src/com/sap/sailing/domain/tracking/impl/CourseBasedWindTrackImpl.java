package com.sap.sailing.domain.tracking.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;
import com.sap.sse.util.impl.ArrayListNavigableSet;
import com.sap.sse.util.impl.UnmodifiableNavigableSet;

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
    private static final long serialVersionUID = -729439204216641791L;

    /**
     * The first leg's direction will be measured this many milliseconds before the estimated race start time,
     * at estimated race start time and this many milliseconds after estimated race start time.
     */
    private final long MILLISECONDS_AROUND_START_TO_TRACK = 30000l;
    
    private final TrackedRace trackedRace;
    private static final NavigableSet<Wind> empty = new UnmodifiableNavigableSet<Wind>(new ArrayListNavigableSet<Wind>(WindComparator.INSTANCE));
    
    public CourseBasedWindTrackImpl(TrackedRace trackedRace, long millisecondsOverWhichToAverage, double baseConfidence) {
        super(millisecondsOverWhichToAverage, baseConfidence,
                /* useSpeed: no usable wind speed information can be extracted from course */ WindSourceType.COURSE_BASED.useSpeed(),
                CourseBasedWindTrackImpl.class.getSimpleName()+" for race "+trackedRace.getRace().getName());
        this.trackedRace = trackedRace;
    }

    @Override
    protected NavigableSet<Wind> getInternalRawFixes() {
        assertReadLock();
        NavigableSet<Wind> result;
        if (trackedRace.raceIsKnownToStartUpwind()) {
            TimePoint startTime = trackedRace.getStartOfRace();
            if (startTime != null) {
                result = new ArrayListNavigableSet<Wind>(3, WindComparator.INSTANCE);
                for (long t = startTime.asMillis() - MILLISECONDS_AROUND_START_TO_TRACK; t <= startTime.asMillis()
                        + MILLISECONDS_AROUND_START_TO_TRACK; t += MILLISECONDS_AROUND_START_TO_TRACK) {
                    final Wind directionFromStartToNextMark = trackedRace.getDirectionFromStartToNextMark(new MillisecondsTimePoint(t));
                    if (directionFromStartToNextMark != null) {
                        result.add(directionFromStartToNextMark);
                    }
                }
            } else {
                result = empty;
            }
        } else {
            result = empty;
        }
        return result;
    }
}
