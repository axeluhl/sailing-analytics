package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.statistics.EventStatisticsDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.StatisticsCalculator;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in the statistics sections for the
 * {@link #GetRegattaStatisticsAction(UUID) given event- and regatta-id}, using a {@link StatisticsCalculator} to prepare
 * the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>5 minutes</i>.
 * </p>
 */
public class GetRegattaStatisticsAction implements SailingAction<ResultWithTTL<EventStatisticsDTO>>, IsClientCacheable, ProvidesLeaderboardRouting {
    
    private UUID eventId;
    private String regattaId;
    
    protected GetRegattaStatisticsAction() {
    }

    /**
     * Creates a {@link GetRegattaStatisticsAction} instance for the given event and regatta-id.
     * 
     * @param eventId {@link UUID} of the event to load races for
     * @param regattaId {@link String id} of the regatta to load races for
     */
    public GetRegattaStatisticsAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<EventStatisticsDTO> execute(SailingDispatchContext context) throws DispatchException {
        StatisticsCalculator statisticsCalculator = new StatisticsCalculator(context.getTrackedRaceStatisticsCache());
        statisticsCalculator.doForLeaderboard(EventActionUtil.getLeaderboardContextWithReadPermissions(context, eventId, regattaId));
        return statisticsCalculator.getResult();
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId).append("_").append(regattaId);
    }

    @Override
    public String getLeaderboardName() {
        return regattaId;
    }
}
