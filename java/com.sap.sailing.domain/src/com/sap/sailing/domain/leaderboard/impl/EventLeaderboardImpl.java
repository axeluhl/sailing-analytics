package com.sap.sailing.domain.leaderboard.impl;

import java.util.List;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.EventLeaderboardGroup;
import com.sap.sailing.domain.leaderboard.Leaderboard;

public class EventLeaderboardImpl extends LeaderboardGroupImpl implements EventLeaderboardGroup {
    private static final long serialVersionUID = -8933204023833087837L;
    private Event event;
    
    public EventLeaderboardImpl(Event event, String name, String description, List<Leaderboard> leaderboards) {
        super(name, description, leaderboards);
        this.event = event;
    }

    @Override
    public Event getEvent() {
       return event;
    }
}
