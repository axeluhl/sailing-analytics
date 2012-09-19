package com.sap.sailing.domain.leaderboard.meta;

import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.base.impl.SimpleAbstractRaceColumn;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MetaLeaderboardColumn extends SimpleAbstractRaceColumn implements RaceColumn {
    private static final long serialVersionUID = 3092096133388262955L;
    private final Leaderboard leaderboard;
    private final Fleet metaFleet;
    
    public MetaLeaderboardColumn(Leaderboard leaderboard, Fleet metaFleet) {
        super();
        this.leaderboard = leaderboard;
        this.metaFleet = metaFleet;
    }

    Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Override
    public String getName() {
        return leaderboard.getName();
    }

    @Override
    public void addRaceColumnListener(RaceColumnListener listener) {
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
    }

    @Override
    public Iterable<? extends Fleet> getFleets() {
        return Collections.singleton(metaFleet);
    }

    @Override
    public Fleet getFleetByName(String fleetName) {
        return fleetName.equals(metaFleet.getName()) ? metaFleet : null;
    }

    @Override
    public Fleet getFleetOfCompetitor(Competitor competitor) {
        return metaFleet;
    }

    @Override
    public void setTrackedRace(Fleet fleet, TrackedRace race) {
    }

    @Override
    public boolean hasTrackedRaces() {
        return false;
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        return null;
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        return null;
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        return null;
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
    }

    @Override
    public boolean isMedalRace() {
        return false;
    }

    @Override
    public void setName(String newName) {
    }

    @Override
    public void releaseTrackedRace(Fleet fleet) {
    }

}
