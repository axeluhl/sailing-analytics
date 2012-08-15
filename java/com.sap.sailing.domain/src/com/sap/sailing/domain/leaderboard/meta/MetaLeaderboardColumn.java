package com.sap.sailing.domain.leaderboard.meta;

import java.util.Collections;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MetaLeaderboardColumn implements RaceColumn {
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
        // TODO Auto-generated method stub
    }

    @Override
    public void removeRaceColumnListener(RaceColumnListener listener) {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasTrackedRaces() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TrackedRace getTrackedRace(Fleet fleet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TrackedRace getTrackedRace(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RaceIdentifier getRaceIdentifier(Fleet fleet) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setRaceIdentifier(Fleet fleet, RaceIdentifier raceIdentifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isMedalRace() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setName(String newName) {
        // TODO Auto-generated method stub

    }

    @Override
    public Pair<Competitor, RaceColumn> getKey(Competitor competitor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void releaseTrackedRace(Fleet fleet) {
        // TODO Auto-generated method stub

    }

}
