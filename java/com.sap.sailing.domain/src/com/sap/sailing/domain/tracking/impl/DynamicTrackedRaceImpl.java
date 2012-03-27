package com.sap.sailing.domain.tracking.impl;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.tracking.DynamicGPSFixTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedEvent;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSTrackListener;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindListener;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace, WindListener, GPSTrackListener<Competitor> {
    private static final Logger logger = Logger.getLogger(DynamicTrackedRaceImpl.class.getName());
    
    private Set<RaceChangeListener> listeners;
    
    private boolean raceIsKnownToStartUpwind;
    
    public DynamicTrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race,
            WindStore windStore, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed,
            long delayForCacheInvalidationOfWindEstimation) {
        super(trackedEvent, race, windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                delayForCacheInvalidationOfWindEstimation);
        this.raceIsKnownToStartUpwind = race.getBoatClass().typicallyStartsUpwind();
        for (Competitor competitor : getRace().getCompetitors()) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
            track.addListener(this);
        }
        // TODO ensure that when a wind source is added or removed, this object is added/removed as listener accordingly
        for (WindSource windSource : getWindSources()) {
            getOrCreateWindTrack(windSource).addListener(this);
        }
    }
    /**
     * {@link #raceIsKnownToStartUpwind} (see also {@link #raceIsKnownToStartUpwind()}) is initialized based on the <code>race</code>'s
     * {@link RaceDefinition#getBoatClass()} boat class's {@link BoatClass#typicallyStartsUpwind()} result. It can be changed
     * using {@link #setRaceIsKnownToStartUpwind(boolean)}. Uses <code>millisecondsOverWhichToAverageWind/2</code> for the
     * <code>delayForCacheInvalidationOfWindEstimation</code> argument of the constructor.
     */
    public DynamicTrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race,
            WindStore windStore,
            long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        this(trackedEvent, race, windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed,
                millisecondsOverWhichToAverageWind/2);
    }

    @Override
    public synchronized void recordFix(Competitor competitor, GPSFixMoving fix) {
        DynamicGPSFixTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
        track.addGPSFix(fix); // the track notifies this tracked race which in turn notifies its listeners
    }
    
    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed; 
        for (Competitor competitor : getRace().getCompetitors()) {
            getTrack(competitor).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
        }
        for (Waypoint waypoint : getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                getOrCreateTrack(buoy).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
            }
        }
        updated(MillisecondsTimePoint.now());
    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
        this.millisecondsOverWhichToAverageWind = millisecondsOverWhichToAverageWind;
        for (WindSource windSource : getWindSources()) {
            getOrCreateWindTrack(windSource).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageWind);
        }
        updated(MillisecondsTimePoint.now());
    }

    @Override
    public DynamicGPSFixTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicGPSFixTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
    }
    
    @Override
    public DynamicGPSFixTrack<Buoy, GPSFix> getOrCreateTrack(Buoy buoy) {
        return (DynamicGPSFixTrack<Buoy, GPSFix>) super.getOrCreateTrack(buoy);
    }
    
    @Override
    protected DynamicGPSFixTrackImpl<Buoy> createBuoyTrack(Buoy buoy) {
        DynamicGPSFixTrackImpl<Buoy> result = super.createBuoyTrack(buoy);
        result.addListener(new GPSTrackListener<Buoy>() {
            @Override
            public void gpsFixReceived(GPSFix fix, Buoy buoy) {
                notifyListeners(fix, buoy);
            }

            @Override
            public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage,
                    long newMillisecondsOverWhichToAverage) {
                // nobody can currently listen for the change of the buoy speed averaging because buoy speed is not a value used
            }
        });
        return result;
    }
    
    /**
     * Callers iterating over the result need to synchronize on the resulting collection while iterating
     * to avoid {@link ConcurrentModificationException}s.
     */
    private Set<RaceChangeListener> getListeners() {
        if (listeners == null) {
            listeners = new HashSet<RaceChangeListener>();
        }
        return listeners;
    }

    @Override
    public void addListener(RaceChangeListener listener) {
    	synchronized (getListeners()) {
    		getListeners().add(listener);
    	}
    }

    private void notifyListeners(GPSFix fix, Buoy buoy) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.buoyPositionChanged(fix, buoy);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(GPSFix, Competitor)", t);
                }
            }
        }
    }

    private void notifyListeners(GPSFix fix, Competitor competitor) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.competitorPositionChanged(fix, competitor);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(GPSFix, Competitor)", t);
                }
            }
        }
    }

    private void notifyListeners(Wind wind) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.windDataReceived(wind);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(Wind)", t);
                }
            }
        }
    }

    private void notifyListenersSpeedAveragingChanged(long oldMillisecondsOverWhichToAverageSpeed, long newMillisecondsOverWhichToAverageSpeed) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.speedAveragingChanged(oldMillisecondsOverWhichToAverageSpeed,
                            newMillisecondsOverWhichToAverageSpeed);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(),
                            "notifyListenersSpeedAveragingChanged(long, long)", t);
                }
            }
        }
    }

    private void notifyListenersWindAveragingChanged(long oldMillisecondsOverWhichToAverageWind, long newMillisecondsOverWhichToAverageWind) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.windAveragingChanged(oldMillisecondsOverWhichToAverageWind,
                            newMillisecondsOverWhichToAverageWind);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(),
                            "notifyListenersWindAveragingChanged(long, long)", t);
                }
            }
        }
    }

    private void notifyListenersWindRemoved(Wind wind) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.windDataRemoved(wind);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListenersWindRemoved(Wind)", t);
                }
            }
        }
    }

    private void notifyListeners(MarkPassing oldMarkPassing, MarkPassing markPassing) {
        synchronized (getListeners()) {
            for (RaceChangeListener listener : getListeners()) {
                try {
                    listener.markPassingReceived(oldMarkPassing, markPassing);
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                    logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(MarkPassing)", t);
                }
            }
        }
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        Map<Waypoint, MarkPassing> oldMarkPassings = new HashMap<Waypoint, MarkPassing>();
        synchronized (this) {
            NavigableSet<MarkPassing> markPassingsForCompetitor = getMarkPassings(competitor);
            synchronized (markPassingsForCompetitor) {
                for (MarkPassing oldMarkPassing : markPassingsForCompetitor) {
                    oldMarkPassings.put(oldMarkPassing.getWaypoint(), oldMarkPassing);
                }
            }
            clearMarkPassings(competitor);
            TimePoint timePointOfLatestEvent = new MillisecondsTimePoint(0);
            for (MarkPassing markPassing : markPassings) {
                synchronized (markPassingsForCompetitor) {
                    markPassingsForCompetitor.add(markPassing);
                }
                Collection<MarkPassing> markPassingsInOrderForWaypoint = getMarkPassingsInOrderAsNavigableSet(markPassing.getWaypoint());
                synchronized (markPassingsInOrderForWaypoint) {
                    markPassingsInOrderForWaypoint.add(markPassing);
                }
                if (markPassing.getTimePoint().compareTo(timePointOfLatestEvent) > 0) {
                    timePointOfLatestEvent = markPassing.getTimePoint();
                }
            }
            updated(timePointOfLatestEvent);
        }
        // notify *after* all mark passings have been re-established; should avoid flicker
        for (MarkPassing markPassing : markPassings) {
            notifyListeners(oldMarkPassings.get(markPassing.getWaypoint()), markPassing);
        }
    }
    
    @Override
    public Iterable<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return (NavigableSet<MarkPassing>) super.getMarkPassingsInOrder(waypoint);
    }

    private void clearMarkPassings(Competitor competitor) {
        NavigableSet<MarkPassing> markPassings = getMarkPassings(competitor);
        synchronized (markPassings) {
            Iterator<MarkPassing> mpIter = markPassings.iterator();
            while (mpIter.hasNext()) {
                MarkPassing mp = mpIter.next();
                mpIter.remove();
                Collection<MarkPassing> markPassingsInOrder = getMarkPassingsInOrderAsNavigableSet(mp.getWaypoint());
                synchronized (markPassingsInOrder) {
                    markPassingsInOrder.remove(mp);
                }
            }
        }
    }

    @Override
    public void setStartTimeReceived(TimePoint start) {
        super.setStartTimeReceived(start);
    }
    
    @Override
    public void setStartOfTrackingReceived(TimePoint startOfTrackingReceived) {
        super.setStartOfTrackingReceived(startOfTrackingReceived);
    }

    @Override
    public void setEndOfTrackingReceived(TimePoint endOfTrackingReceived) {
        super.setEndOfTrackingReceived(endOfTrackingReceived);
    }

    /**
     * In addition to calling the super class implementation, adds this tracked race as a listener for the wind track.
     */
    protected WindTrack createWindTrack(WindSource windSource) {
        WindTrack result = super.createWindTrack(windSource);
        result.addListener(this);
        return result;
    }

    @Override
    public synchronized void recordWind(Wind wind, WindSource windSource) {
        getOrCreateWindTrack(windSource).add(wind);
        updated(null); // wind events shouldn't advance race time
    }
    
    @Override
    public synchronized void removeWind(Wind wind, WindSource windSource) {
        getOrCreateWindTrack(windSource).remove(wind);
        updated(wind.getTimePoint());
    }

    @Override
    public void gpsFixReceived(GPSFix fix, Competitor competitor) {
        updated(fix.getTimePoint());
        notifyListeners(fix, competitor);
    }

    @Override
    public void speedAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        notifyListenersSpeedAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage);
    }

    @Override
    public void windAveragingChanged(long oldMillisecondsOverWhichToAverage, long newMillisecondsOverWhichToAverage) {
        notifyListenersWindAveragingChanged(oldMillisecondsOverWhichToAverage, newMillisecondsOverWhichToAverage);        
    }

    @Override
    public void windDataReceived(Wind wind) {
        notifyListeners(wind);
    }

    @Override
    public void windDataRemoved(Wind wind) {
        notifyListenersWindRemoved(wind);
    }


    @Override
    protected TrackedLeg createTrackedLeg(Leg leg) {
        return new TrackedLegImpl(this, leg, getRace().getCompetitors());
    }

    @Override
    public long getMillisecondsOverWhichToAverageSpeed() {
        long result = 0; // default in case there is no competitor
        Iterator<Competitor> compIter = getRace().getCompetitors().iterator();
        if (compIter.hasNext()) {
            DynamicGPSFixTrack<Competitor, GPSFixMoving> someTrack = getTrack(compIter.next());
            result = someTrack.getMillisecondsOverWhichToAverageSpeed();
        }
        return result;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        long result = 0; // default in case there is no competitor
        for (WindSource windSource : getWindSources()) {
            WindTrack someTrack = getOrCreateWindTrack(windSource);
            result = someTrack.getMillisecondsOverWhichToAverageWind();
        }
        return result;
    }

    @Override
    public DynamicTrackedEvent getTrackedEvent() {
        return (DynamicTrackedEvent) super.getTrackedEvent();
    }

    @Override
    public void setRaceIsKnownToStartUpwind(boolean raceIsKnownToStartUpwind) {
        this.raceIsKnownToStartUpwind = raceIsKnownToStartUpwind;
    }

    @Override
    public boolean raceIsKnownToStartUpwind() {
        return raceIsKnownToStartUpwind;
    }

}
