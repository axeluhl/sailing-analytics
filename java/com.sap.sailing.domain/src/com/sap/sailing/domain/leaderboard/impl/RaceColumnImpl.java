package com.sap.sailing.domain.leaderboard.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.RaceColumn;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceColumnImpl implements RaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    
    /**
     * All fleets for which this column can contain a race. This is the maximum set of keys possible for
     * {@link #trackedRaces} and {@link #raceIdentifiers}.
     */
    private final Iterable<Fleet> fleets;
    private final Map<Fleet, TrackedRace> trackedRaces;
    private boolean medalRace;
    private String name;
    private final Map<Fleet, RaceIdentifier> raceIdentifiers;
    
    public RaceColumnImpl(String name, boolean medalRace, Iterable<Fleet> fleets) {
        this.name = name;
        this.medalRace = medalRace;
        this.fleets = fleets;
        this.trackedRaces = new HashMap<Fleet, TrackedRace>();
        this.raceIdentifiers = new HashMap<Fleet, RaceIdentifier>();
    }
    
    @Override
    public boolean isMedalRace() {
        return medalRace;
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return trackedRaces.get(fleet);
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace trackedRace) {
        this.trackedRaces.put(fleet, trackedRace);
        this.setRaceIdentifier(fleet, trackedRace == null ? null : trackedRace.getRaceIdentifier());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        return new Pair<Competitor, RaceColumn>(competitor, this);
    }

    @Override
    public void setIsMedalRace(boolean isMedalRace) {
        this.medalRace = isMedalRace;
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return raceIdentifiers.get(fleet);
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
        this.raceIdentifiers.put(fleet, raceIdentifier);
    }

    @Override
    public void releaseTrackedRace(Fleet fleet) {
        trackedRaces.remove(fleet);
    }

    @Override
    public Iterable<Fleet> getFleets() {
        return fleets;
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        for (Fleet fleet : getFleets()) {
            if (fleet.getName().equals(fleetName)) {
                return fleet;
            }
        }
        return null;
    }

    @Override
    public boolean hasTrackedRaces() {
        return !trackedRaces.isEmpty();
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        for (TrackedRace trackedRace : trackedRaces.values()) {
            if (Util.contains(trackedRace.getRace().getCompetitors(), competitor)) {
                return trackedRace;
            }
        }
        return null;
    }
}
