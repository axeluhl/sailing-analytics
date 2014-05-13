package com.sap.sailing.domain.markpassingcalculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;

/**
 * Listens for changes that might affect the MarkPassingCalculator: new Fixes of a Competitor or a Mark and when the
 * race status changes to finished. New Fixes are put in queue to be evaluated by the {@link MarkPassingCalculator} and
 * the <code>end</code> object is passed through to signal the end of the race.
 * 
 * @author Nicolas Klose
 * 
 */
public class MarkPassingUpdateListener extends AbstractRaceChangeListener implements CourseListener {
    private LinkedBlockingQueue<StorePositionUpdateStrategy> queue;
    private final StorePositionUpdateStrategy endMarker = new StorePositionUpdateStrategy() {
        @Override
        public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixMarkPassing,
                List<MarkPassing> removeFixedMarkPassing) {
        }
    };

    /**
     * Adds itself automatically as a Listener on the <code>race</code> and its course.
     */
    public MarkPassingUpdateListener(TrackedRace race) {
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
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
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
    public void markPositionChanged(final GPSFix fix, final Mark mark) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
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
        queue.add(endMarker);
    }

    @Override
    public void waypointAdded(final int zeroBasedIndex, final Waypoint waypointThatGotAdded) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
                addedWaypoints.put(waypointThatGotAdded, zeroBasedIndex);
            }
        });
    }

    @Override
    public void waypointRemoved(final int zeroBasedIndex, final Waypoint waypointThatGotRemoved) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
                removedWaypoints.put(waypointThatGotRemoved, zeroBasedIndex);
            }
        });
    }

    public void addFixedPassing(final MarkPassing m) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixedMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
                fixedMarkPassing.add(m);
            }
        });
    }

    public void removeFixedPassing(final MarkPassing m) {
        queue.add(new StorePositionUpdateStrategy() {
            @Override
            public void storePositionUpdate(Map<Competitor, List<GPSFix>> competitorFixes,
                    Map<Mark, List<GPSFix>> markFixes, Map<Waypoint, Integer> addedWaypoints,
                    Map<Waypoint, Integer> removedWaypoints, List<MarkPassing> fixedMarkPassing,
                    List<MarkPassing> removeFixedMarkPassing) {
                removeFixedMarkPassing.add(m);

            }

        });
    }

}
