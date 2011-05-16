package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Gate;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.GPSFix;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedEvent;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedEventImpl implements TrackedEvent {
    private final Event event;
    private final Map<RaceDefinition, TrackedRace> trackedRaces;
    private final Map<BoatClass, Collection<TrackedRace>> trackedRacesByBoatClass;
    private final Map<Buoy, Track<Buoy, GPSFix>> buoyTracks;
  
    public TrackedEventImpl(Event event) {
        super();
        this.event = event;
        this.trackedRaces = new HashMap<RaceDefinition, TrackedRace>();
        this.trackedRacesByBoatClass = new HashMap<BoatClass, Collection<TrackedRace>>();
        buoyTracks = new HashMap<Buoy, Track<Buoy, GPSFix>>();
    }
    
    @Override
    public void addTrackedRace(TrackedRace trackedRace) {
        trackedRaces.put(trackedRace.getRace(), trackedRace);
        Collection<TrackedRace> coll = trackedRacesByBoatClass.get(trackedRace.getRace().getBoatClass());
        if (coll == null) {
            coll = new ArrayList<TrackedRace>();
            trackedRacesByBoatClass.put(trackedRace.getRace().getBoatClass(), coll);
        }
        for (Waypoint waypoint : trackedRace.getRace().getCourse().getWaypoints()) {
            // TODO add Waypoint.getBuoys()
            if (waypoint instanceof Buoy) {
                buoyTracks.put((Buoy) waypoint, new DynamicTrackImpl<Buoy, GPSFix>((Buoy) waypoint));
            } else {
                buoyTracks.put(((Gate) waypoint).getLeft(), new DynamicTrackImpl<Buoy, GPSFix>(((Gate) waypoint).getLeft()));
                buoyTracks.put(((Gate) waypoint).getRight(), new DynamicTrackImpl<Buoy, GPSFix>(((Gate) waypoint).getRight()));
            }
        }
        coll.add(trackedRace);
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
    public Track<Buoy, GPSFix> getTrack(Buoy buoy) {
        return buoyTracks.get(buoy);
    }

}
