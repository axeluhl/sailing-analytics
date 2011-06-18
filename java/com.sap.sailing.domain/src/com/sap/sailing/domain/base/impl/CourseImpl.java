package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;

public class CourseImpl extends NamedImpl implements Course {
    private static final Logger logger = Logger.getLogger(CourseImpl.class.getName());
    
    private final List<Waypoint> waypoints;
    private final Map<Waypoint, Integer> waypointIndexes;
    private final List<Leg> legs;
    private final Set<CourseListener> listeners;
    
    public CourseImpl(String name, Iterable<Waypoint> waypoints) {
        super(name);
        listeners = new HashSet<CourseListener>();
        this.waypoints = new ArrayList<Waypoint>();
        waypointIndexes = new HashMap<Waypoint, Integer>();
        legs = new ArrayList<Leg>();
        Iterator<Waypoint> waypointIter = waypoints.iterator();
        int i=0;
        if (waypointIter.hasNext()) {
            Waypoint previous = waypointIter.next();
            this.waypoints.add(previous);
            
            waypointIndexes.put(previous, i++);
            while (waypointIter.hasNext()) {
                Waypoint current = waypointIter.next();
                this.waypoints.add(current);
                
                int indexOfStartWaypoint = i-1;
                waypointIndexes.put(current, i++);
                Leg leg = new LegImpl(this, indexOfStartWaypoint);
                legs.add(leg);
                previous = current;
            }
        }
    }
    
    /**
     * For access by {@link LegImpl}
     */
    Waypoint getWaypoint(int i) {
        return waypoints.get(i);
    }
    
    @Override
    public synchronized void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd) {
        waypoints.add(zeroBasedPosition, waypointToAdd);
        int legStartWaypointIndex;
        if (zeroBasedPosition == waypoints.size()-1) {   // added to end
            legStartWaypointIndex = zeroBasedPosition-1;
        } else {
            legStartWaypointIndex = zeroBasedPosition;
        }
        legs.add(new LegImpl(this, legStartWaypointIndex));
        notifyListenersWaypointAdded(zeroBasedPosition, waypointToAdd);
    }

    @Override
    public synchronized void removeWaypoint(int zeroBasedPosition) {
        if (zeroBasedPosition >= 0) {
            boolean isLast = zeroBasedPosition == waypoints.size()-1;
            Waypoint removedWaypoint = waypoints.remove(zeroBasedPosition);
            if (isLast) {
                // last waypoint was removed; remove last leg
                legs.remove(legs.size()-1);
            } else {
                legs.remove(zeroBasedPosition);
            }
            notifyListenersWaypointRemoved(zeroBasedPosition, removedWaypoint);
        }
    }

    private void notifyListenersWaypointRemoved(int index, Waypoint waypointToRemove) {
        for (CourseListener listener : listeners) {
            try {
                listener.waypointRemoved(index, waypointToRemove);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception while notifying listener about waypoint " + waypointToRemove
                        + " that got removed from course " + this + ": " + t.getMessage());
                logger.throwing(CourseImpl.class.getName(), "notifyListenersWaypointRemoved", t);
            }
        }
    }

    private void notifyListenersWaypointAdded(int zeroBasedPosition, Waypoint waypointToAdd) {
        for (CourseListener listener : listeners) {
            try {
                listener.waypointAdded(zeroBasedPosition, waypointToAdd);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "Exception while notifying listener about waypoint " + waypointToAdd
                        + " that got added to course " + this + ": " + t.getMessage());
                logger.throwing(CourseImpl.class.getName(), "notifyListenersWaypointAdded", t);
            }
        }
    }

    @Override
    public synchronized List<Leg> getLegs() {
        return Collections.unmodifiableList(legs);
    }

    @Override
    public synchronized Iterable<Waypoint> getWaypoints() {
        return new ArrayList<Waypoint>(waypoints);
    }

    @Override
    public synchronized String toString() {
        StringBuilder result = new StringBuilder(getName());
        result.append(": ");
        boolean first = true;
        for (Waypoint waypoint : getWaypoints()) {
            if (!first) {
                result.append(" -> ");
            } else {
                first = false;
            }
            result.append(waypoint);
        }
        return result.toString();
    }

    @Override
    public int getIndexOfWaypoint(Waypoint waypoint) {
        int result = -1;
        Integer indexEntry = waypointIndexes.get(waypoint);
        if (indexEntry != null) {
            result = indexEntry;
        }
        return result;
    }

    @Override
    public Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
        if (start > legs.size()) {
            throw new IllegalArgumentException("Starting to search beyond end of course: "+start+" vs. "+(legs.size()+1));
        }
        int i=0;
        for (Waypoint waypoint : getWaypoints()) {
            if (i >= start && waypoint.getControlPoint() == controlPoint) {
                return waypoint;
            }
            i++;
        }
        return null;
    }

    @Override
    public synchronized Waypoint getFirstWaypoint() {
        return waypoints.get(0);
    }

    @Override
    public synchronized Waypoint getLastWaypoint() {
        return waypoints.get(waypoints.size()-1);
    }

    @Override
    public void addCourseListener(CourseListener listener) {
        listeners.add(listener);
    }
    
}
