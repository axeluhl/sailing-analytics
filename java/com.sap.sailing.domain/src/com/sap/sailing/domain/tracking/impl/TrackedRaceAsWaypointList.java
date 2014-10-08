package com.sap.sailing.domain.tracking.impl;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util;

/**
 * Represents a {@link TrackedRace} object as a list of {@link Waypoint}s. When waypoints are added or removed to the
 * list, the calls are mapped to {@link TrackedRaceImpl#waypointAdded(int, Waypoint)} and
 * {@link TrackedRaceImpl#waypointRemoved(int, Waypoint)} calls, respectively.
 * <p>
 * 
 * Background: Under very special circumstances, particularly during serialization/de-serialization (see bug 2223 at
 * http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2223), a tracked race's internal structures may become
 * inconsistent with those of the {@link Course} it references through its {@link TrackedRace#getRace() RaceDefinition}.
 * In this case, there may be extra {@link TrackedLeg} and {@link TrackedLegOfCompetitor} structures or they may be
 * missing, depending on whether waypoints were added or removed during serialization. The case with excess elements
 * is a bit tricky because the {@link Leg} objects used by the tracked race are in their default implementation only
 * an index into the {@link Course}'s waypoint list, and this index may be out of bounds for excess legs. The waypoints
 * now missing from the {@link Course} object are, however, still contained as keys in the tracked race's mark passing
 * collections.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public class TrackedRaceAsWaypointList extends AbstractList<Waypoint> implements Serializable {
    private static final long serialVersionUID = 7167163814845413202L;
    private final TrackedRaceImpl trackedRace;

    public TrackedRaceAsWaypointList(TrackedRaceImpl trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public Waypoint get(int index) {
        final Waypoint result;
        final int numberOfLegs = Util.size(trackedRace.getTrackedLegs());
        final Iterable<Waypoint> courseWaypoints = trackedRace.getRace().getCourse().getWaypoints();
        final boolean isConsistentWithCourse = numberOfLegs == Util.size(courseWaypoints)-1;
        if (index > numberOfLegs) {
            result = null;
        } else {
            if (isConsistentWithCourse) {
                // in this case it's safe to access the legs' waypoints
                if (index == numberOfLegs) {
                    if (numberOfLegs == 0) {
                        // only one waypoint --> no leg; get the only waypoint
                        result = trackedRace.getWaypoints().iterator().next();
                    } else {
                        result = Util.get(trackedRace.getTrackedLegs(), numberOfLegs - 1).getLeg().getTo();
                    }
                } else {
                    result = Util.get(trackedRace.getTrackedLegs(), index).getLeg().getFrom();
                }
            } else {
                // we have to assume that there may be excess legs that don't match any of the course's waypoints;
                // therefore, the waypoint list needs to be assembled from the course's waypoints, augmented by
                // the excess waypoints returned from trackedRace.getWaypoints().
                // The order of the excess waypoints will arbitrarily depend on the mark roundings key set iteration order,
                // but that will remain constant as long as no waypoints are added to or removed from the tracked race.
                Set<Waypoint> excessWaypoints = new LinkedHashSet<>();
                Util.addAll(trackedRace.getWaypoints(), excessWaypoints);
                List<Waypoint> waypoints = new ArrayList<>();
                Util.addAll(courseWaypoints, waypoints);
                Util.removeAll(courseWaypoints, excessWaypoints);
                waypoints.addAll(excessWaypoints);
                result = waypoints.get(index);
            }
        }
        return result;
    }

    @Override
    public int size() {
        return Util.size(trackedRace.getWaypoints());
    }

    @Override
    public void add(int index, Waypoint element) {
        trackedRace.waypointAdded(index, element);
    }

    @Override
    public Waypoint remove(int zeroBasedIndex) {
        final Waypoint waypointThatGotRemoved = get(zeroBasedIndex);
        if (waypointThatGotRemoved != null) {
            trackedRace.waypointRemoved(zeroBasedIndex, waypointThatGotRemoved);
        }
        return waypointThatGotRemoved;
    }

}
