package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.LeaderboardBase;
import com.sap.sailing.domain.base.LeaderboardGroupBase;
import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.LeaderboardSearchResultBase;
import com.sap.sailing.domain.leaderboard.Leaderboard;

/**
 * The simplified version of a {@link LeaderboardSearchResult}, after JSON deserialization on a remote server. In particular,
 * the {@link Leaderboard} object is most certainly not available on the receiving end. {@link EventBase} and {@link LeaderboardGroupBase}
 * objects are 
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class LeaderboardSearchResultBaseImpl implements LeaderboardSearchResultBase {
    private final LeaderboardBase leaderboard;
    private final String regattaName;
    private final Iterable<? extends LeaderboardGroupBase> leaderboardGroups;
    private final Iterable<? extends EventBase> events;
    private final String boatClassName;
    
    public LeaderboardSearchResultBaseImpl(LeaderboardBase leaderboard, String regattaName,
            String boatClassName, Iterable<? extends LeaderboardGroupBase> leaderboardGroups, Iterable<? extends EventBase> events) {
        super();
        this.leaderboard = leaderboard;
        this.regattaName = regattaName;
        this.boatClassName = boatClassName;
        this.leaderboardGroups = leaderboardGroups;
        this.events = events;
    }

    @Override
    public LeaderboardBase getLeaderboard() {
        return leaderboard;
    }

    @Override
    public String getRegattaName() {
        return regattaName;
    }

    @Override
    public String getBoatClassName() {
        return boatClassName;
    }

    @Override
    public Iterable<? extends LeaderboardGroupBase> getLeaderboardGroups() {
        return leaderboardGroups;
    }

    @Override
    public Iterable<? extends EventBase> getEvents() {
        return events;
    }

}
