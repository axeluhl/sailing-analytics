package com.sap.sailing.gwt.home.server;

import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sailing.server.statistics.TrackedRaceStatisticsCache;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * {@link LeaderboardCallback} implementation, which calculates the statistics for the regatta(s) passed to the
 * {@link #doForLeaderboard(LeaderboardContext)} method.
 */
public class StatisticsCalculator extends com.sap.sailing.server.statistics.StatisticsCalculator
        implements LeaderboardCallback {

    public StatisticsCalculator(TrackedRaceStatisticsCache trackedRaceStatisticsCache) {
        super(trackedRaceStatisticsCache);
    }

    @Override
    public void doForLeaderboard(LeaderboardContext context) {
        addLeaderboard(context.getLeaderboard());
    }

    /**
     * @return the {@link ResultWithTTL} containing the calculated statistics.
     */
    public ResultWithTTL<EventStatisticsDTO> getResult() {
        Distance totalDistanceTraveled = getTotalDistanceTraveled();
        totalDistanceTraveled = totalDistanceTraveled == Distance.NULL ? null : totalDistanceTraveled;
        return new ResultWithTTL<EventStatisticsDTO>(Duration.ONE_MINUTE.times(5),
                new EventStatisticsDTO(getNumberOfRegattas(), getNumberOfCompetitors(), getNumberOfRaces(),
                        getNumberOfTrackedRaces(), getNumberOfGPSFixes(), getNumberOfWindFixes(), getMaxSpeed(),
                        totalDistanceTraveled));
    }

}
