package com.sap.sailing.dashboards.gwt.server.util.actions.startlineadvantage.precalculation;

import java.util.Iterator;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface PreCalculationMarkRetriever {
    
    default StartlineAndFirstMarkPositions retrieveMarkPositions(TrackedRace trackedRace) {
        StartlineAndFirstMarkPositions result = new StartlineAndFirstMarkPositions();
        Course course = trackedRace.getRace().getCourse();
        if (course != null) {
            Waypoint startlineWayPoint = course.getFirstLeg().getFrom();
            Waypoint firstmarkWayPoint = course.getFirstLeg().getTo();
            if (startlineWayPoint != null && firstmarkWayPoint != null) {
                Pair<Position, Position> startlineMarkPositions = retrieveStartlineMarkPositionsFromStartLineWayPoint(startlineWayPoint, trackedRace);
                Position firstMarkPosition = retrieveFirstMarkPositionFromFirstMarkWayPoint(firstmarkWayPoint, trackedRace);
                result.startBoatPosition = startlineMarkPositions.getA();
                result.pinEndPosition = startlineMarkPositions.getB();
                result.firstMarkPosition = firstMarkPosition;
            }
        }
        return result;
    }

    default Pair<Position, Position> retrieveStartlineMarkPositionsFromStartLineWayPoint(Waypoint startLineWayPoint, TrackedRace trackedRace) {
        Pair<Position, Position> result = null;
        Iterator<Mark> markIterator = startLineWayPoint.getMarks().iterator();
        if (markIterator.hasNext()) {
            Mark startboat = (Mark) markIterator.next();
            if (markIterator.hasNext()) {
                Mark pinEnd = (Mark) markIterator.next();
                TimePoint now = MillisecondsTimePoint.now();
                Position startBoatPosition = getPositionFromMarkAtTimePoint(trackedRace, startboat, now);
                Position pinEndPosition = getPositionFromMarkAtTimePoint(trackedRace, pinEnd, now);
                result = new Pair<Position, Position>(startBoatPosition, pinEndPosition);
            }
        }
        return result;
    }

    default Position retrieveFirstMarkPositionFromFirstMarkWayPoint(Waypoint firstMarkWayPoint, TrackedRace trackedRace) {
        Position result = null;
        if (firstMarkWayPoint.getMarks().iterator().hasNext()) {
            Mark firstMark = firstMarkWayPoint.getMarks().iterator().next();
            TimePoint now = MillisecondsTimePoint.now();
            result = getPositionFromMarkAtTimePoint(trackedRace, firstMark, now);
        }
        return result;
    }

    default Position getPositionFromMarkAtTimePoint(TrackedRace trackedRace, Mark mark, TimePoint timePoint) {
        GPSFixTrack<Mark, GPSFix> fixTrack = trackedRace.getTrack(mark);
        return fixTrack.getEstimatedPosition(timePoint, true);
    }
    
    class StartlineAndFirstMarkPositions {
        public Position startBoatPosition;
        public Position pinEndPosition;
        public Position firstMarkPosition;
    }
}
