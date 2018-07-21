package com.sap.sailing.gwt.home.communication.event.statistics;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.StatisticsCalculator;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the statistics sections for the
 * {@link #GetEventStatisticsAction(UUID) given event-id}, using a {@link StatisticsCalculator} to prepare the
 * appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>5 minutes</i>.
 * </p>
 */
public class GetEventStatisticsAction implements SailingAction<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable {
    
    private UUID eventId;

    protected GetEventStatisticsAction() {
    }

    /**
     * Creates a {@link GetEventStatisticsAction} instance for the given event-id.
     * 
     * @param eventId
     *            {@link UUID} of the event to load data for
     */
    public GetEventStatisticsAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(SailingDispatchContext context) throws DispatchException {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator(context.getTrackedRaceStatisticsCache());
        EventActionUtil.forLeaderboardsOfEvent(context, eventId, statisticsCalculator);
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
    }
}
