package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.RaceListener;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedEventImpl implements TrackedEvent {
    private final Event event;
    private final Map<RaceDefinition, TrackedRace> trackedRaces;
    private final Map<BoatClass, Collection<TrackedRace>> trackedRacesByBoatClass;
    private final Map<Buoy, GPSFixTrack<Buoy, GPSFix>> buoyTracks;
    private final Set<RaceListener> raceListeners;
    private final long millisecondsOverWhichToAverageSpeed;
  
    public TrackedEventImpl(Event event, long millisecondsOverWhichToAverageSpeed) {
        super();
        this.event = event;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        this.trackedRacesByBoatClass = new HashMap<BoatClass, Collection<TrackedRace>>();
        buoyTracks = new HashMap<Buoy, GPSFixTrack<Buoy, GPSFix>>();
        raceListeners = new HashSet<RaceListener>();
        this.millisecondsOverWhichToAverageSpeed = millisecondsOverWhichToAverageSpeed;
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
            for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
                for (Buoy buoy : waypoint.getBuoys()) {
                    if (!buoyTracks.containsKey(buoy)) {
                        buoyTracks.put(buoy, new DynamicTrackImpl<Buoy, GPSFix>(buoy, millisecondsOverWhichToAverageSpeed));
                    }
                }
            }
            coll.add(trackedRace);
            for (RaceListener listener : raceListeners) {
                listener.raceAdded(trackedRace);
            }
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
        return trackedRaces.get(race);
    }

    @Override
    public GPSFixTrack<Buoy, GPSFix> getTrack(Buoy buoy) {
        GPSFixTrack<Buoy, GPSFix> result = buoyTracks.get(buoy);
        if (result == null) {
            result = new DynamicTrackImpl<Buoy, GPSFix>(buoy, millisecondsOverWhichToAverageSpeed);
            buoyTracks.put(buoy, result);
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
    public int getTotalPoints(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getNetPoints(Competitor competitor) {
        // TODO Auto-generated method stub
        return 0;
    }

}
