package com.sap.sailing.server.impl;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.LeaderboardMXBean;
import com.sap.sse.common.Util;

public class LeaderboardMXBeanImpl implements LeaderboardMXBean {
    private static final long serialVersionUID = -8420830339429971378L;
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
    
    @Override
    public String getName() {
        return leaderboard.getName();
    }

    private String escapeIllegalObjectNameCharacters(String name) {
        return name.replaceAll("[:/,]", "_");
    }

    @Override
    public int getNumberOfCompetitors() {
        return Util.size(leaderboard.getCompetitors());
    }

    @Override
    public int getNumberOfAllCompetitors() {
        return Util.size(leaderboard.getAllCompetitors());
    }

    @Override
    public String getDisplayName() {
        return leaderboard.getDisplayName();
    }

    @Override
    public String getType() {
        return leaderboard.getLeaderboardType().name();
    }
    
    @Override
    public long getDelayToLiveInMillis() {
        return leaderboard.getDelayToLiveInMillis();
    }

    @Override
    public String getBoatClass() {
        return leaderboard.getBoatClass().getName();
    }
}
