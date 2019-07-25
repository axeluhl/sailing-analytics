package com.sap.sailing.server.impl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.LeaderboardMXBean;

public class LeaderboardMXBeanImpl implements LeaderboardMXBean {
    private final Leaderboard leaderboard;

    public LeaderboardMXBeanImpl(Leaderboard leaderboard) {
        super();
        this.leaderboard = leaderboard;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    @Override
    public ObjectName getObjectName() throws MalformedObjectNameException {
        return new ObjectName("com.sap.sailing:type=Leaderboard,name="+escapeIllegalObjectNameCharacters(leaderboard.getName()));
    }

    private String escapeIllegalObjectNameCharacters(String name) {
        return name.replaceAll("[:/,]", "_");
    }
}
