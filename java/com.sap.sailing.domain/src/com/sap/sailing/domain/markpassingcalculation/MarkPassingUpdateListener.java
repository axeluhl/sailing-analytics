package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;

/**
 * Listens for changes that might affect the MarkPassingCalculator: new Fixes of a Competitor or a Mark and when the
 * race status changes to finished. New Fixes are put in queue to be evaluated by the {@link MarkPassingCalculator} and
 * the <code>end</code> object is passed through to signal the end of the race.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingUpdateListener extends AbstractRaceChangeListener {
    private static final Logger logger = Logger.getLogger(MarkPassingUpdateListener.class.getName());
    private LinkedBlockingQueue<StorePositionUpdateStrategy> queue;
    private final StorePositionUpdateStrategy endMarker = new StorePositionUpdateStrategy() {
        @Override
        public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
        }
    };

    /**
     * Adds itself automatically as a Listener on the <code>race</code> and its course.
     */
    public MarkPassingUpdateListener(DynamicTrackedRace race) {
        queue = new LinkedBlockingQueue<>();
        race.addListener(this);
        race.getRace().getCourse().addCourseListener(this);
    }

    public BlockingQueue<StorePositionUpdateStrategy> getQueue() {
        return queue;
    }

    @Override
    public void competitorPositionChanged(final GPSFixMoving fix, final Competitor competitor) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                List<GPSFix> list = competitorFixes.get(competitor);
                if (list == null) {
                    list = new ArrayList<>();
                    competitorFixes.put(competitor, list);
                }
                list.add(fix);
            }
        });
    }

    @Override
    public void markPositionChanged(final GPSFix fix, final Mark mark, boolean firstInTrack) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                List<GPSFix> list = markFixes.get(mark);
                if (list == null) {
                    list = new ArrayList<>();
                    markFixes.put(mark, list);
                }
                list.add(fix);
            }
        });
    }

    public boolean isEndMarker(StorePositionUpdateStrategy endMarkerCandidate) {
        return endMarkerCandidate == endMarker;
    }

    public void stop() {
        logger.info("Stopping "+this);
        queue.add(endMarker);
    }

    @Override
    public void waypointAdded(final int zeroBasedIndex, final Waypoint waypointThatGotAdded) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                addedWaypoints.add(waypointThatGotAdded);
                if (smallestChangedWaypointIndex == null || smallestChangedWaypointIndex > zeroBasedIndex) {
                    smallestChangedWaypointIndex = zeroBasedIndex;
                }
            }
        });
    }

    @Override
    public void waypointRemoved(final int zeroBasedIndex, final Waypoint waypointThatGotRemoved) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                removedWaypoints.add(waypointThatGotRemoved);
                if (smallestChangedWaypointIndex == null || smallestChangedWaypointIndex > zeroBasedIndex) {
                    smallestChangedWaypointIndex = zeroBasedIndex;
                }
            }
        });
    }

    public void addFixedPassing(final Competitor c, final Integer zeroBasedIndexOfWaypoint, final TimePoint timePointOfFixedPassing) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                fixedMarkPassings.add(new Triple<Competitor, Integer, TimePoint>(c, zeroBasedIndexOfWaypoint, timePointOfFixedPassing));
            }
        });
    }

    public void removeFixedPassing(final Competitor c, final Integer zeroBasedIndexOfWaypoint) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                removedMarkPassings.add(new Pair<Competitor, Integer>(c, zeroBasedIndexOfWaypoint));
            }

        });
    }

    public void addSuppressedPassing(final Competitor c, final Integer zeroBasedIndexOfWaypoint) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                suppressedMarkPassings.add(new Pair<Competitor, Integer>(c, zeroBasedIndexOfWaypoint));
                
            }
        });
    }

    public void removeSuppressedPassing(final Competitor c) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes, Map<Mark, List<GPSFix>> markFixes,
                    List<Waypoint> addedWaypoints, List<Waypoint> removedWaypoints, Integer smallestChangedWaypointIndex,
                    List<Triple<Competitor, Integer, TimePoint>> fixedMarkPassings, List<Pair<Competitor, Integer>> removedMarkPassings,
                    List<Pair<Competitor, Integer>> suppressedMarkPassings, List<Competitor> unSuppressedMarkPassings) {
                unSuppressedMarkPassings.add(c);
            }
        });
    }
}
