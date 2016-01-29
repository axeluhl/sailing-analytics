package com.sap.sailing.domain.base.impl;

import java.util.Collections;
import java.util.Set;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;

public class LeaderboardSearchResultImpl implements LeaderboardSearchResult {
    private final Leaderboard leaderboard;
    private final Set<Event> events;
    private final Set<LeaderboardGroup> leaderboardGroups;
    
    public LeaderboardSearchResultImpl(Leaderboard leaderboard, Set<Event> events, Set<LeaderboardGroup> leaderboardGroups) {
        this.leaderboard = leaderboard;
        if (leaderboardGroups == null) {
            this.leaderboardGroups = Collections.emptySet();
        } else {
            this.leaderboardGroups = leaderboardGroups;
        }
        this.events = events;
    }

    @Override
    public Regatta getRegatta() {
        final Regatta regatta;
        if (leaderboard instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
        } else {
            regatta = null;
        }
        return regatta;
    }
    
    @Override
    public String getRegattaName() {
        return getRegatta() != null ? getRegatta().getName() : null;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public Iterable<Event> getEvents() {
        return events;
    }

    @Override
    public Iterable<LeaderboardGroup> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    @Override
    public String getBoatClassName() {
        BoatClass boatClass = getBoatClass();
        return boatClass == null ? null : boatClass.getName();
    }

    public BoatClass getBoatClass() {
        final BoatClass boatClass;
        if (getRegatta() != null) {
            boatClass = getRegatta().getBoatClass();
        } else {
            boatClass = getBoatClassFromTrackedRaces();
        }
        return boatClass;
    }

    private BoatClass getBoatClassFromTrackedRaces() {
        for (TrackedRace trackedRace : getLeaderboard().getTrackedRaces()) {
            return trackedRace.getRace().getBoatClass();
        }
        return null;
    }
}
