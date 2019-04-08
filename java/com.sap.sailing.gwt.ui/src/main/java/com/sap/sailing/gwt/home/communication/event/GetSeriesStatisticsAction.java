package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.communication.event.statistics.GetEventStatisticsAction;
import com.sap.sailing.gwt.home.server.StatisticsCalculator;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the statistics sections for the
 * {@link #GetSeriesStatisticsAction(UUID) given series-id}, using a {@link StatisticsCalculator} to prepare the
 * appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>5 minutes</i>.
 * </p>
 */
public class GetSeriesStatisticsAction implements SailingAction<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable {
    
    private UUID seriesLeaderboardGroupId;
    
    protected GetSeriesStatisticsAction() {
    }

    /**
     * Creates a {@link GetEventStatisticsAction} instance for the given series-id.
     * 
     * @param seriesId
     *            {@link UUID} of the series to load data for
     */
    public GetSeriesStatisticsAction(UUID seriesLeaderboardGroupId) {
        this.seriesLeaderboardGroupId = seriesLeaderboardGroupId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(SailingDispatchContext context) throws DispatchException {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator(context.getTrackedRaceStatisticsCache());
        final LeaderboardGroup leaderboardGroupByID = context.getRacingEventService().getLeaderboardGroupByID(seriesLeaderboardGroupId);
        leaderboardGroupByID.getLeaderboards().forEach(statisticsCalculator::addLeaderboard);
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesLeaderboardGroupId);
    }
}
