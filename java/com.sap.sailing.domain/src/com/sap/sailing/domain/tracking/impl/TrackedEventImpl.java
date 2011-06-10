package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.NoWindException;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedEventImpl implements TrackedEvent {
    private final Event event;
    private final Map<RaceDefinition, TrackedRace> trackedRaces;
    private final Map<BoatClass, Collection<TrackedRace>> trackedRacesByBoatClass;
    private final Set<RaceListener> raceListeners;
  
    public TrackedEventImpl(Event event) {
        super();
        this.event = event;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        this.trackedRacesByBoatClass = new HashMap<BoatClass, Collection<TrackedRace>>();
        raceListeners = new HashSet<RaceListener>();
    }
    
    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        synchronized (trackedRaces) {
            trackedRaces.put(trackedRace.getRace(), trackedRace);
            Collection<TrackedRace> coll = trackedRacesByBoatClass.get(trackedRace.getRace().getBoatClass());
            if (coll == null) {
                coll = new ArrayList<TrackedRace>();
                trackedRacesByBoatClass.put(trackedRace.getRace().getBoatClass(), coll);
            }
            coll.add(trackedRace);
            for (RaceListener listener : raceListeners) {
                listener.raceAdded(trackedRace);
            }
            trackedRaces.notifyAll();
        }
    }

    @Override
    public Event getEvent() {
        return event;
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces() {
        return trackedRaces.values();
    }

    @Override
    public Iterable<TrackedRace> getTrackedRaces(BoatClass boatClass) {
        return trackedRacesByBoatClass.get(boatClass);
    }

    @Override
    public TrackedRace getTrackedRace(RaceDefinition race) {
        TrackedRace result = trackedRaces.get(race);
        boolean interrupted = false;
        synchronized (trackedRaces) {
            while (!interrupted && result == null) {
                try {
                    trackedRaces.wait();
                    result = trackedRaces.get(race);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        }
        return result;
    }

    @Override
    public void addRaceListener(RaceListener listener) {
        raceListeners.add(listener);
        synchronized (trackedRaces) {
            for (TrackedRace trackedRace : getTrackedRaces()) {
                listener.raceAdded(trackedRace);
            }
        }
    }

    @Override
    public int getTotalPoints(Competitor competitor, TimePoint timePoint) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNetPoints(Competitor competitor, TimePoint timePoint) throws NoWindException {
        int result = 0;
        for (TrackedRace trackedRace : getTrackedRaces()) {
            result += trackedRace.getRank(competitor, timePoint);
        }
        return result;
    }

}
