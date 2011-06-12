package com.sap.sailing.domain.tracking.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.base.Waypoint;
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
    public DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
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
    public void setFinish(TimePoint firstFinish) {
        super.setFinish(firstFinish);
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
}
