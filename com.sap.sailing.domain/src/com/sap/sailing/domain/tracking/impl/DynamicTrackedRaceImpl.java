package com.sap.sailing.domain.tracking.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.tracking.DynamicTrack;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.RawListener;
import com.sap.sailing.domain.tracking.TrackedRace;

public class DynamicTrackedRaceImpl extends TrackedRaceImpl implements
        DynamicTrackedRace, RawListener<GPSFixMoving> {
    private final Map<Competitor, List<GPSFixMoving>> fixes;
    private final Set<RawListener<GPSFixMoving>> listeners;
    
    public DynamicTrackedRaceImpl(RaceDefinition race) {
        super(race);
        fixes = new HashMap<Competitor, List<GPSFixMoving>>();
        listeners = new HashSet<RawListener<GPSFixMoving>>();
        for (Competitor competitor : getRace().getCompetitors()) {
            DynamicTrack<Competitor, GPSFixMoving> track = getTrack(competitor);
            track.addListener(this);
        }
    }

    @Override
    public void recordFix(Competitor competitor, GPSFixMoving fix) {
        List<GPSFixMoving> list = fixes.get(competitor);
        if (list == null) {
            list = new ArrayList<GPSFixMoving>();
            fixes.put(competitor, list);
        }
        list.add(fix);
        notifyListeners(fix, this, competitor);
    }

    @Override
    public DynamicTrack<Competitor, GPSFixMoving> getTrack(Competitor competitor) {
        return (DynamicTrack<Competitor, GPSFixMoving>) super.getTrack(competitor);
    }

    @Override
    public void addListener(RawListener<GPSFixMoving> listener) {
        listeners.add(listener);
    }

    private void notifyListeners(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        for (RawListener<GPSFixMoving> listener : listeners) {
            listener.gpsFixReceived(fix, trackedRace, competitor);
        }
    }

    @Override
    public void gpsFixReceived(GPSFixMoving fix, TrackedRace trackedRace, Competitor competitor) {
        notifyListeners(fix, trackedRace, competitor);
    }
    
}
