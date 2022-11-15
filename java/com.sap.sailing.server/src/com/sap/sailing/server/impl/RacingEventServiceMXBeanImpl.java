package com.sap.sailing.server.impl;

import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.server.LeaderboardMXBean.ComputationTimeAverage;
import com.sap.sailing.server.RacingEventServiceMXBean;
import com.sap.sailing.server.impl.LeaderboardMXBeanImpl.ComputationTimeAverageImpl;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class RacingEventServiceMXBeanImpl implements RacingEventServiceMXBean {
    private final RacingEventService racingEventService;

    protected RacingEventServiceMXBeanImpl(RacingEventService racingEventService) {
        super();
        this.racingEventService = racingEventService;
    }
    
    private RacingEventService getRacingEventService() {
        return racingEventService;
    }

    @Override
    public int getNumberOfLeaderboards() {
        return getRacingEventService().getLeaderboards().size();
    }

    @Override
    public long getNumberOfTrackedRacesToRestore() {
        return getRacingEventService().getNumberOfTrackedRacesToRestore();
    }

    @Override
    public int getNumberOfTrackedRacesRestored() {
        return getRacingEventService().getNumberOfTrackedRacesRestored();
    }
    
    @Override
    public ObjectName[] getLeaderboards() throws MalformedObjectNameException {
        final ObjectName[] result = new ObjectName[getRacingEventService().getLeaderboards().size()];
        int i=0;
        for (final Entry<String, Leaderboard> entry : getRacingEventService().getLeaderboards().entrySet()) {
            result[i++] = new LeaderboardMXBeanImpl(entry.getValue()).getObjectName();
        }
        return result;
    }
    
    @Override
    public ComputationTimeAverage getLeaderboardComputationStatisticsYoung() {
        return getLeaderboardComputationStatistics(0);
    }

    @Override
    public ComputationTimeAverage getLeaderboardComputationStatisticsMedium() {
        return getLeaderboardComputationStatistics(1);
    }

    @Override
    public ComputationTimeAverage getLeaderboardComputationStatisticsOld() {
        return getLeaderboardComputationStatistics(2);
    }

    private ComputationTimeAverage getLeaderboardComputationStatistics(int indexInStatsSortedByAscendingSampleDuration) {
        final TreeMap<Duration, Pair<AtomicLong, AtomicInteger>> computationDurationSumsInMillisPerSamplingDuration = new TreeMap<>();
        for (final Leaderboard leaderboard : getRacingEventService().getLeaderboards().values()) {
            for (final Entry<Duration, Pair<Duration, Integer>> leaderboardStats : leaderboard.getComputationTimeStatistics().entrySet()) {
                if (leaderboardStats.getValue().getA() != null) {
                    final long computationTimeForLeaderboardInRangeInMillis = leaderboardStats.getValue().getA().asMillis() * leaderboardStats.getValue().getB();
                    computationDurationSumsInMillisPerSamplingDuration.compute(leaderboardStats.getKey(),
                            (final Duration key, final Pair<AtomicLong, AtomicInteger> oldComputationDurationSumAndCount)->{
                                final Pair<AtomicLong, AtomicInteger> newValue;
                                if (oldComputationDurationSumAndCount == null) {
                                    newValue = new Pair<>(new AtomicLong(computationTimeForLeaderboardInRangeInMillis), new AtomicInteger(leaderboardStats.getValue().getB()));
                                } else {
                                    oldComputationDurationSumAndCount.getA().addAndGet(computationTimeForLeaderboardInRangeInMillis);
                                    oldComputationDurationSumAndCount.getB().addAndGet(leaderboardStats.getValue().getB());
                                    newValue = oldComputationDurationSumAndCount;
                                }
                                return newValue;
                            });
                }
            }
        }
        final Entry<Duration, Pair<AtomicLong, AtomicInteger>> relevantEntry = Util.get(
                computationDurationSumsInMillisPerSamplingDuration.entrySet(), indexInStatsSortedByAscendingSampleDuration);
        return new ComputationTimeAverageImpl(relevantEntry.getKey().asMillis(),
                relevantEntry.getValue().getA().get() / relevantEntry.getValue().getB().get(), relevantEntry.getValue().getB().get());
    }
}
