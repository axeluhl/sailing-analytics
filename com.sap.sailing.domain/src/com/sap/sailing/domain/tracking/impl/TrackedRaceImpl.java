package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Track;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class TrackedRaceImpl implements TrackedRace {
    private final RaceDefinition race;
    private TimePoint start;
    private TimePoint firstFinish;
    private final Iterable<TrackedLeg> trackedLegs;
    private final Map<Competitor, Track<Competitor, GPSFixMoving>> tracks;
    
    public TrackedRaceImpl(RaceDefinition race) {
        super();
        this.race = race;
        ArrayList<TrackedLeg> trackedLegsList = new ArrayList<TrackedLeg>();
        for (Leg leg : race.getCourse().getLegs()) {
            trackedLegsList.add(new TrackedLegImpl(leg, race.getCompetitors()));
        }
        trackedLegs = trackedLegsList;
        tracks = new HashMap<Competitor, Track<Competitor, GPSFixMoving>>();
        for (Competitor competitor : race.getCompetitors()) {
            tracks.put(competitor, new DynamicTrackImpl<Competitor, GPSFixMoving>(competitor));
        }
    }

    @Override
    public TimePoint getStart() {
        return start;
    }


    public void setStart(TimePoint start) {
        this.start = start;
    }


    @Override
    public TimePoint getFirstFinish() {
        return firstFinish;
    }


    public void setFirstFinish(TimePoint firstFinish) {
        this.firstFinish = firstFinish;
    }


    @Override
    public RaceDefinition getRace() {
        return race;
    }


    @Override
    public Iterable<TrackedLeg> getTrackedLegs() {
        return trackedLegs;
    }

    @Override
    public Track<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return tracks.get(competitor);
    }

}
