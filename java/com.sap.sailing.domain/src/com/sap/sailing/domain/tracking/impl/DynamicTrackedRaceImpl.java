package com.sap.sailing.domain.tracking.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.RaceChangeListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.WindSource;
import com.sap.sailing.domain.tracking.WindStore;
import com.sap.sailing.domain.tracking.WindTrack;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace, RaceChangeListener<Competitor> {
    private static final Logger logger = Logger.getLogger(DynamicTrackedRaceImpl.class.getName());
    
    private Set<RaceChangeListener<Competitor>> listeners;
    
    public DynamicTrackedRaceImpl(TrackedEvent trackedEvent, RaceDefinition race,
            WindStore windStore, long millisecondsOverWhichToAverageWind, long millisecondsOverWhichToAverageSpeed) {
        super(trackedEvent, race, windStore, millisecondsOverWhichToAverageWind, millisecondsOverWhichToAverageSpeed);
        for (Competitor competitor : getRace().getCompetitors()) {
            DynamicTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
            track.addListener(this);
        }
        for (WindSource windSource : WindSource.values()) {
            getWindTrack(windSource).addListener(this);
        }
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        DynamicTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
        track.addGPSFix(fix); // the track notifies this tracked race which in turn notifies its listeners
        updated(fix.getTimePoint());
    }
    
    @Override
    public void setMillisecondsOverWhichToAverageSpeed(long millisecondsOverWhichToAverageSpeed) {
        for (Competitor competitor : getRace().getCompetitors()) {
            getTrack(competitor).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
        }
        for (Waypoint waypoint : getRace().getCourse().getWaypoints()) {
            for (Buoy buoy : waypoint.getBuoys()) {
                getTrack(buoy).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageSpeed);
            }
        }
        updated(MillisecondsTimePoint.now());
    }

    @Override
    public void setMillisecondsOverWhichToAverageWind(long millisecondsOverWhichToAverageWind) {
        for (WindSource windSource : WindSource.values()) {
            getWindTrack(windSource).setMillisecondsOverWhichToAverage(millisecondsOverWhichToAverageWind);
        }
        updated(MillisecondsTimePoint.now());
    }

    @Override
    public DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
    }
    
    @Override
    public DynamicTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
        return (DynamicTrack<Buoy, GPSFix>) super.getTrack(buoy);
    }
    
    private synchronized Set<RaceChangeListener<Competitor>> getListeners() {
        if (listeners == null) {
            listeners = new HashSet<RaceChangeListener<Competitor>>();
        }
        return listeners;
    }

    @Override
    public synchronized void addListener(RaceChangeListener<Competitor> listener) {
        getListeners().add(listener);
    }

    private void notifyListeners(GPSFix fix, Competitor competitor) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.gpsFixReceived(fix, competitor);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener "+listener+" threw exception "+t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(GPSFix, Competitor)", t);
            }
        }
    }

    private void notifyListeners(Wind wind) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.windDataReceived(wind);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(Wind)", t);
            }
        }
    }

    private void notifyListenersSpeedAveragingChanged(long oldMillisecondsOverWhichToAverageSpeed, long newMillisecondsOverWhichToAverageSpeed) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.speedAveragingChanged(oldMillisecondsOverWhichToAverageSpeed, newMillisecondsOverWhichToAverageSpeed);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListenersSpeedAveragingChanged(long, long)", t);
            }
        }
    }

    private void notifyListenersWindAveragingChanged(long oldMillisecondsOverWhichToAverageWind, long newMillisecondsOverWhichToAverageWind) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.windAveragingChanged(oldMillisecondsOverWhichToAverageWind, newMillisecondsOverWhichToAverageWind);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListenersWindAveragingChanged(long, long)", t);
            }
        }
    }

    private void notifyListenersWindRemoved(Wind wind) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.windDataRemoved(wind);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener " + listener + " threw exception " + t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListenersWindRemoved(Wind)", t);
            }
        }
    }

    private void notifyListeners(MarkPassing markPassing) {
        for (RaceChangeListener<Competitor> listener : getListeners()) {
            try {
                listener.markPassingReceived(markPassing);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "RaceChangeListener "+listener+" threw exception "+t.getMessage());
                logger.throwing(DynamicTrackedRaceImpl.class.getName(), "notifyListeners(MarkPassing)", t);
            }
        }
    }

    @Override
    public void updateMarkPassings(Competitor competitor, Iterable<MarkPassing> markPassings) {
        clearMarkPassings(competitor);
        NavigableSet<MarkPassing> competitorMarkPassings = getMarkPassings(competitor);
        for (MarkPassing markPassing : markPassings) {
            competitorMarkPassings.add(markPassing);
            getMarkPassingsInOrder(markPassing.getWaypoint()).add(markPassing);
            updated(markPassing.getTimePoint());
        }
        // notify *after* all mark passings have been re-established; should avoid flicker
        for (MarkPassing markPassing : markPassings) {
            notifyListeners(markPassing);
        }
    }
    
    @Override
    public Collection<MarkPassing> getMarkPassingsInOrder(Waypoint waypoint) {
        return (Collection<MarkPassing>) super.getMarkPassingsInOrder(waypoint);
    }

    private void clearMarkPassings(Competitor competitor) {
        Iterator<MarkPassing> mpIter = getMarkPassings(competitor).iterator();
        while (mpIter.hasNext()) {
            MarkPassing mp = mpIter.next();
            mpIter.remove();
            getMarkPassingsInOrder(mp.getWaypoint()).remove(mp);
        }
    }

    @Override
    public void setStart(TimePoint start) {
        super.setStart(start);
    }

    @Override
    public void recordWind(Wind wind, WindSource windSource) {
        getWindTrack(windSource).add(wind);
        updated(wind.getTimePoint());
    }
    
    @Override
    public void removeWind(Wind wind, WindSource windSource) {
        getWindTrack(windSource).remove(wind);
        updated(wind.getTimePoint());
    }

    @Override
    public void gpsFixReceived(GPSFix fix, Competitor competitor) {
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
    public void markPassingReceived(MarkPassing markPassing) {
        notifyListeners(markPassing);
        
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
            DynamicTrack<Competitor, GPSFixMoving> someTrack = getTrack(compIter.next());
            result = someTrack.getMillisecondsOverWhichToAverageSpeed();
        }
        return result;
    }

    @Override
    public long getMillisecondsOverWhichToAverageWind() {
        long result = 0; // default in case there is no competitor
        for (WindSource windSource : WindSource.values()) {
            WindTrack someTrack = getWindTrack(windSource);
            result = someTrack.getMillisecondsOverWhichToAverageWind();
        }
        return result;
    }

}
