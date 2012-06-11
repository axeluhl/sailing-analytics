package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.util.CourseAsWaypointList;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class CourseImpl extends NamedImpl implements Course {
    private static final long serialVersionUID = -4280487649617132403L;

    private static final Logger logger = Logger.getLogger(CourseImpl.class.getName());
    
    private final List<Waypoint> waypoints;
    private final Map<Waypoint, Integer> waypointIndexes;
    private final List<Leg> legs;
    private transient Set<CourseListener> listeners;
    
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
    
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<>();
    }
    
    /**
     * Synchronize on this object to avoid concurrent modifications of the underlying waypoints list
     */
    private synchronized void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
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
        Map<Waypoint, Integer> updatesToWaypointIndexes = new HashMap<Waypoint, Integer>();
        updatesToWaypointIndexes.put(waypointToAdd, zeroBasedPosition);
        for (Map.Entry<Waypoint, Integer> e : waypointIndexes.entrySet()) {
            if (e.getValue() >= zeroBasedPosition) {
                updatesToWaypointIndexes.put(e.getKey(), e.getValue()+1);
            }
        }
        waypointIndexes.putAll(updatesToWaypointIndexes);
        int legStartWaypointIndex;
        if (zeroBasedPosition == waypoints.size()-1) {   // added to end
            legStartWaypointIndex = zeroBasedPosition-1;
        } else {
            legStartWaypointIndex = zeroBasedPosition;
        }
        if (waypoints.size() > 1) {
            legs.add(new LegImpl(this, legStartWaypointIndex));
        }
        notifyListenersWaypointAdded(zeroBasedPosition, waypointToAdd);
    }

    @Override
    public synchronized void removeWaypoint(int zeroBasedPosition) {
        if (zeroBasedPosition >= 0) {
            boolean isLast = zeroBasedPosition == waypoints.size()-1;
            Waypoint removedWaypoint = waypoints.remove(zeroBasedPosition);
            waypointIndexes.remove(removedWaypoint);
            Map<Waypoint, Integer> updatesToWaypointIndexes = new HashMap<Waypoint, Integer>();
            for (Map.Entry<Waypoint, Integer> e : waypointIndexes.entrySet()) {
                if (e.getValue() > zeroBasedPosition) { // only > because the entry with == was just removed
                    updatesToWaypointIndexes.put(e.getKey(), e.getValue()-1);
                }
            }
            waypointIndexes.putAll(updatesToWaypointIndexes);
            if (isLast) {
                if (waypoints.size() > 0) { // if we had only one waypoint, we didn't have any legs
                    // last waypoint was removed; remove last leg
                    legs.remove(legs.size() - 1);
                }
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
        return new ArrayList<Leg>(legs);
    }

    @Override
    public Iterable<Waypoint> getWaypoints() {
        return new ArrayList<Waypoint>(waypoints);
    }

    @Override
    public String toString() {
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
    public synchronized int getIndexOfWaypoint(Waypoint waypoint) {
        int result = -1;
        Integer indexEntry = waypointIndexes.get(waypoint);
        if (indexEntry != null) {
            result = indexEntry;
        }
        return result;
    }
    
    private Set<ControlPoint> getControlPoints() {
        Set<ControlPoint> result = new HashSet<ControlPoint>();
        for (Waypoint waypoint : getWaypoints()) {
            result.add(waypoint.getControlPoint());
        }
        return result;
    }
    
    private ControlPoint getControlPointForBuoy(Buoy buoy) {
        for (ControlPoint controlPoint : getControlPoints()) {
            for (Buoy controlPointBuoy : controlPoint.getBuoys()) {
                if (buoy == controlPointBuoy) {
                    return controlPoint;
                }
            }
        }
        return null;
    }
    
    @Override
    public Iterable<Leg> getLegsAdjacentTo(Buoy buoy) {
        Set<Leg> result = new HashSet<Leg>();
        ControlPoint controlPointForBuoy = getControlPointForBuoy(buoy);
        if (controlPointForBuoy != null) {
            boolean first = true;
            for (Leg leg : getLegs()) {
                if (first) {
                    if (leg.getFrom().getControlPoint() == controlPointForBuoy) {
                        result.add(leg);
                    }
                    first = false;
                }
                if (leg.getTo().getControlPoint() == controlPointForBuoy) {
                    result.add(leg);
                }
            }
        }
        return result;
    }

    @Override
    public synchronized Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
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
    public Waypoint getFirstWaypoint() {
        return waypoints.get(0);
    }

    @Override
    public Waypoint getLastWaypoint() {
        return waypoints.get(waypoints.size()-1);
    }

    @Override
    public void addCourseListener(CourseListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void removeCourseListener(CourseListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void update(List<ControlPoint> newControlPoints, DomainFactory baseDomainFactory) throws PatchFailedException {
        Iterable<Waypoint> courseWaypoints = getWaypoints();
        List<Waypoint> newWaypointList = new LinkedList<Waypoint>();
        // key existing waypoints by control points and re-use each one at most once during construction of the
        // new waypoint list; since several waypoints can have the same control point, the map goes from
        // control point to List<Waypoint>. The waypoints in the lists are held in the order of their
        // occurrence in courseToUpdate.getWaypoints().
        Map<com.sap.sailing.domain.base.ControlPoint, List<Waypoint>> existingWaypointsByControlPoint =
                new HashMap<com.sap.sailing.domain.base.ControlPoint, List<Waypoint>>();
        for (Waypoint waypoint : courseWaypoints) {
            List<Waypoint> wpl = existingWaypointsByControlPoint.get(waypoint.getControlPoint());
            if (wpl == null) {
                wpl = new ArrayList<Waypoint>();
                existingWaypointsByControlPoint.put(waypoint.getControlPoint(), wpl);
            }
            wpl.add(waypoint);
        }
        for (com.sap.sailing.domain.base.ControlPoint newDomainControlPoint : newControlPoints) {
            List<Waypoint> waypoints = existingWaypointsByControlPoint.get(newDomainControlPoint);
            Waypoint waypoint;
            if (waypoints == null || waypoints.isEmpty()) {
                // must be a new control point for which we don't have a waypoint yet
                waypoint = baseDomainFactory.createWaypoint(newDomainControlPoint);
            } else {
                waypoint = waypoints.remove(0); // take the first from the list
            }
            newWaypointList.add(waypoint);
        }
        Patch<Waypoint> patch = DiffUtils.diff(courseWaypoints, newWaypointList);
        CourseAsWaypointList courseAsWaypointList = new CourseAsWaypointList(this);
        synchronized (this) {
            patch.applyToInPlace(courseAsWaypointList);
        }
    }
    
}
