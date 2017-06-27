package com.sap.sailing.gwt.home.server;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil.LeaderboardCallback;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * {@link LeaderboardCallback} implementation, which calculates the statistics for the regatta(s) passed to the
 * {@link #doForLeaderboard(LeaderboardContext)} method. Because this calculations can be costly and time consuming,
 * several information can be disabled/enabled:
 * <ul>
 * <li>Maximum speed (of a competitor) - by static flag {@link #CALCULATE_MAX_SPEED} within this class</li>
 * <li>Total of sailed miles - by static flag {@link #CALCULATE_SAILED_MILES} within this class</li>
 * <li>Information from tracked races - by environment variable <code>DISABLE_STATS</code></li>
 * </ul>
 */
public class StatisticsCalculator extends com.sap.sailing.server.statistics.StatisticsCalculator
        implements LeaderboardCallback {

    private static final boolean CALCULATE_MAX_SPEED = false;
    private static final boolean CALCULATE_SAILED_MILES = true;

    public StatisticsCalculator() {
        super(CALCULATE_MAX_SPEED, CALCULATE_SAILED_MILES);
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
