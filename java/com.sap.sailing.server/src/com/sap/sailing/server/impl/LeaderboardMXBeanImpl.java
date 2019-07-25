package com.sap.sailing.server.impl;

import java.util.Map;
import java.util.Map.Entry;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.LeaderboardMXBean;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

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
        return leaderboard.getDelayToLiveInMillis() == null ? -1 : leaderboard.getDelayToLiveInMillis();
    }

    @Override
    public String getBoatClass() {
        return leaderboard.getBoatClass().getName();
    }

    public static class ComputationTimeAverageImpl implements ComputationTimeAverage {
        private final long averageRangeInMillis;
        private final long averageComputeDurationInMillis;
        private final int numberOfComputations;
        public ComputationTimeAverageImpl(long averageRangeInMillis, long averageComputeDurationInMillis,
                int numberOfComputations) {
            super();
            this.averageRangeInMillis = averageRangeInMillis;
            this.averageComputeDurationInMillis = averageComputeDurationInMillis;
            this.numberOfComputations = numberOfComputations;
        }
        @Override
        public long getAverageRangeInMillis() {
            return averageRangeInMillis;
        }
        @Override
        public long getAverageComputeDurationInMillis() {
            return averageComputeDurationInMillis;
        }
        @Override
        public int getNumberOfComputations() {
            return numberOfComputations;
        }
    }
    
    @Override
    public ComputationTimeAverage[] getComputationTimeAverages() {
        final Map<Duration, Pair<Duration, Integer>> computationTimeStatistics = getLeaderboard().getComputationTimeStatistics();
        final ComputationTimeAverage[] result = new ComputationTimeAverage[computationTimeStatistics.size()];
        int i=0;
        for (final Entry<Duration, Pair<Duration, Integer>> e : computationTimeStatistics.entrySet()) {
            result[i++] = new ComputationTimeAverageImpl(e.getKey().asMillis(),
                                                         e.getValue().getA()==null?-1:e.getValue().getA().asMillis(),
                                                         e.getValue().getB());
        }
        return result;
    }
}
