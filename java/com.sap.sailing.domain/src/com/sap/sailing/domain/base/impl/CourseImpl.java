package com.sap.sailing.domain.base.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.ControlPoint;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.CourseListener;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.PassingInstruction;
import com.sap.sailing.util.CourseAsWaypointList;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.RenamableImpl;
import com.sap.sse.concurrent.LockUtil;
import com.sap.sse.concurrent.NamedReentrantReadWriteLock;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

public class CourseImpl extends RenamableImpl implements Course {

    private static final long serialVersionUID = -4280487649617132403L;

    private static final Logger logger = Logger.getLogger(CourseImpl.class.getName());
    
    private final List<Waypoint> waypoints;
    private final Map<Waypoint, Integer> waypointIndexes;
    private final List<Leg> legs;
    private transient Set<CourseListener> listeners;
    private transient NamedReentrantReadWriteLock lock;
    
    /**
     * This monitor is used to serialize calls to {@link #update(List, DomainFactory)}. This helps to guarantee that
     * the transition from a read lock to a write lock, in case a write is actually needed, isn't interrupted by any other
     * call to {@link #update(List, DomainFactory)}.
     */
    private final Serializable updateMonitor;
    
    public CourseImpl(String name, Iterable<Waypoint> waypoints) {
        super(name);
        updateMonitor = ""+new Random().nextDouble(); 
        lock = new NamedReentrantReadWriteLock("lock for CourseImpl "+name,
                /* fair */ true); // if non-fair, course update may need to wait forever for many concurrent readers
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
        assert this.waypoints.size() == waypointIndexes.size();
    }
    
    @Override
    public void lockForRead() {
        LockUtil.lockForRead(lock);
    }

    @Override
    public void unlockAfterRead() {
        LockUtil.unlockAfterRead(lock);
    }
    
    public void lockForWrite() {
        LockUtil.lockForWrite(lock);
    }
    
    public void unlockAfterWrite() {
        LockUtil.unlockAfterWrite(lock);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        listeners = new HashSet<>();
        lock = new NamedReentrantReadWriteLock("lock for CourseImpl "+this.getName(), /* fair */ true);
    }
    
    /**
     * Synchronize on this object to avoid concurrent modifications of the underlying waypoints list
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        lockForRead();
        try {
            s.defaultWriteObject();
        } finally {
            unlockAfterRead();
        }
    }
    
    /**
     * For access by {@link LegImpl}
     */
    Waypoint getWaypoint(int i) {
        return waypoints.get(i);
    }
    
    @Override
    public void addWaypoint(int zeroBasedPosition, Waypoint waypointToAdd) {
        LockUtil.lockForWrite(lock);
        try {
            assert !waypoints.contains(waypointToAdd); // no duplicate waypoints allowed
            logger.info("Adding waypoint " + waypointToAdd + " to course '" + getName() + "'");
            waypoints.add(zeroBasedPosition, waypointToAdd);
            Map<Waypoint, Integer> updatesToWaypointIndexes = new HashMap<Waypoint, Integer>();
            updatesToWaypointIndexes.put(waypointToAdd, zeroBasedPosition);
            for (Map.Entry<Waypoint, Integer> e : waypointIndexes.entrySet()) {
                if (e.getValue() >= zeroBasedPosition) {
                    updatesToWaypointIndexes.put(e.getKey(), e.getValue() + 1);
                }
            }
            waypointIndexes.putAll(updatesToWaypointIndexes);
            // legs are "virtual" in that they only contain a waypoint index; adding happens most conveniently by
            // appending a leg with its start waypoint index pointing to the last but one waypoint, leaving all others unchanged
            if (waypoints.size() > 1) {
                legs.add(new LegImpl(this, waypoints.size()-2));
            }
            logger.info("Waypoint " + waypointToAdd + " added to course '" + getName() + "', before notifying listeners");
            notifyListenersWaypointAdded(zeroBasedPosition, waypointToAdd);
            logger.info("Waypoint " + waypointToAdd + " added to course '" + getName() + "', after notifying listeners");
            assert waypoints.size() == waypointIndexes.size();
        } finally {
            LockUtil.unlockAfterWrite(lock);
        }
    }

    @Override
    public void removeWaypoint(int zeroBasedPosition) {
        if (zeroBasedPosition >= 0) {
            Waypoint removedWaypoint;
            LockUtil.lockForWrite(lock);
            try {
                removedWaypoint = waypoints.remove(zeroBasedPosition);
                logger.info("Removing waypoint " + removedWaypoint + " from course '" + getName() + "'");
                waypointIndexes.remove(removedWaypoint);
                Map<Waypoint, Integer> updatesToWaypointIndexes = new HashMap<Waypoint, Integer>();
                for (Map.Entry<Waypoint, Integer> e : waypointIndexes.entrySet()) {
                    if (e.getValue() > zeroBasedPosition) { // only > because the entry with == was just removed
                        updatesToWaypointIndexes.put(e.getKey(), e.getValue() - 1);
                    }
                }
                waypointIndexes.putAll(updatesToWaypointIndexes);
                // the legs are "virtual" only in that they contain a waypoint index; when removing, removing the last is most
                // convenient because all other legs' indices will still be contiguous
                if (!legs.isEmpty()) { // if we had only one waypoint, we didn't have any legs
                    // last waypoint was removed; remove last leg
                    legs.remove(legs.size() - 1);
                }
                logger.info("Waypoint " + removedWaypoint + " removed from course '" + getName() + "', before notifying listeners");
                notifyListenersWaypointRemoved(zeroBasedPosition, removedWaypoint);
                logger.info("Waypoint " + removedWaypoint + " removed from course '" + getName() + "', after notifying listeners");
                assert waypoints.size() == waypointIndexes.size();
            } finally {
                LockUtil.unlockAfterWrite(lock);
            }
        }
    }

    private void notifyListenersWaypointRemoved(int index, Waypoint waypointToRemove) {
        for (CourseListener listener : listeners) {
            try {
                listener.waypointRemoved(index, waypointToRemove);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while notifying listener about waypoint " + waypointToRemove
                        + " that got removed from course " + this + ": " + e.getMessage());
                logger.log(Level.SEVERE, "notifyListenersWaypointRemoved", e);
            }
        }
    }

    private void notifyListenersWaypointAdded(int zeroBasedPosition, Waypoint waypointToAdd) {
        for (CourseListener listener : listeners) {
            try {
                listener.waypointAdded(zeroBasedPosition, waypointToAdd);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exception while notifying listener about waypoint " + waypointToAdd
                        + " that got added to course " + this + ": " + e.getMessage());
                logger.log(Level.SEVERE, "notifyListenersWaypointAdded", e);
            }
        }
    }

    @Override
    public Leg getFirstLeg() {
        Leg result = null;
        if (!legs.isEmpty()) {
            try {
                result = legs.get(0);
            } catch (IndexOutOfBoundsException e) {
                // The legs collection could have turned empty since the check; we don't want to have to lock here...
                // Yes, it's a bit clumsy, but this way it's still atomic without requiring a lock which is even more expensives
            }
        }
        return result;
    }

    @Override
    public List<Leg> getLegs() {
        lockForRead();
        try {
            return new ArrayList<Leg>(legs);
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Iterable<Waypoint> getWaypoints() {
        lockForRead();
        try {
            return new ArrayList<Waypoint>(waypoints);
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public int getNumberOfWaypoints() {
        return waypoints.size();
    }

    @Override
    public Leg getLeg(int zeroBasedIndexOfWaypoint) {
        return legs.get(zeroBasedIndexOfWaypoint);
    }

    @Override
    public String toString() {
        lockForRead();
        try {
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
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public int getIndexOfWaypoint(Waypoint waypoint) {
        lockForRead();
        try {
            int result = -1;
            Integer indexEntry = waypointIndexes.get(waypoint);
            if (indexEntry != null) {
                result = indexEntry;
            }
            return result;
        } finally {
            unlockAfterRead();
        }
    }
    
    @Override
    public Waypoint getWaypointForControlPoint(ControlPoint controlPoint, int start) {
        lockForRead();
        try {
            if (start > legs.size()) {
                throw new IllegalArgumentException("Starting to search beyond end of course: " + start + " vs. "
                        + (legs.size() + 1));
            }
            int i = 0;
            for (Waypoint waypoint : getWaypoints()) {
                if (i >= start && waypoint.getControlPoint() == controlPoint) {
                    return waypoint;
                }
                i++;
            }
            return null;
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Waypoint getFirstWaypoint() {
        lockForRead();
        try {
            return waypoints.isEmpty() ? null : waypoints.get(0);
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    public Waypoint getLastWaypoint() {
        lockForRead();
        try {
            return waypoints.isEmpty() ? null : waypoints.get(waypoints.size() - 1);
        } finally {
            unlockAfterRead();
        }
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
    public void update(Iterable<Pair<ControlPoint, PassingInstruction>> newControlPoints, DomainFactory baseDomainFactory) throws PatchFailedException {
        Patch<Waypoint> patch = null;
        synchronized (updateMonitor) {
            lockForRead();
            try {
                Iterable<Waypoint> courseWaypoints = getWaypoints();
                List<Waypoint> newWaypointList = new LinkedList<Waypoint>();
                // key existing waypoints by (ControlPoint, PassingInstruction) pairs and re-use them during construction of the
                // new waypoint list; since several waypoints can have the same control point, the map goes from
                // Pair<ControlPoint, PassingInstruction> to List<Waypoint>. The waypoints in the lists are held in the order of their
                // occurrence in courseToUpdate.getWaypoints().
                final Map<Pair<com.sap.sailing.domain.base.ControlPoint, PassingInstruction>, List<Waypoint>> existingWaypointsByControlPoint = new HashMap<>();
                for (Waypoint waypoint : courseWaypoints) {
                    final Pair<ControlPoint, PassingInstruction> key = new Pair<>(waypoint.getControlPoint(), waypoint.getPassingInstructions());
                    List<Waypoint> wpl = existingWaypointsByControlPoint.get(key);
                    if (wpl == null) {
                        wpl = new ArrayList<Waypoint>();
                        existingWaypointsByControlPoint.put(key, wpl);
                    }
                    wpl.add(waypoint);
                }
                for (Pair<ControlPoint, PassingInstruction> newDomainControlPoint : newControlPoints) {
                    List<Waypoint> waypoints = existingWaypointsByControlPoint.get(new Pair<>(newDomainControlPoint.getA(), newDomainControlPoint.getB()));
                    Waypoint waypoint;
                    if (waypoints == null || waypoints.isEmpty()) {
                        // must be a new control point for which we don't have a waypoint yet
                        waypoint = baseDomainFactory.createWaypoint(newDomainControlPoint.getA(), newDomainControlPoint.getB());
                    } else {
                        waypoint = waypoints.remove(0); // take the first from the list
                    }
                    newWaypointList.add(waypoint);
                }
                patch = DiffUtils.diff(courseWaypoints, newWaypointList);
            } finally {
                unlockAfterRead();
            }
            if (patch != null && !patch.isEmpty()) {
                lockForWrite();
                try {
                    logger.info("applying course update " + patch + " to course " + this);
                    CourseAsWaypointList courseAsWaypointList = new CourseAsWaypointList(this);
                    patch.applyToInPlace(courseAsWaypointList);
                } finally {
                    unlockAfterWrite();
                }
            }
        }
    }
}
