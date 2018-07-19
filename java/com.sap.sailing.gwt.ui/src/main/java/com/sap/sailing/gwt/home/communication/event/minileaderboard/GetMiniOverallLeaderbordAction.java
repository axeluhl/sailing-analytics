package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in compact leaderboard (e.g. on the series overview
 * page) for the {@link #GetMiniOverallLeaderbordAction(UUID) given series-id}, where the amount of loaded entries can
 * optionally be {@link #GetMiniOverallLeaderbordAction(UUID, int) limited}.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>1 minute</i> for currently running events, <i>2 minutes</i>
 * otherwise.
 * </p>
 */
public class GetMiniOverallLeaderbordAction implements SailingAction<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable, ProvidesLeaderboardRouting {
    private UUID seriesId;
    private String leaderboardName;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniOverallLeaderbordAction() {
    }

    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given series-id, where the amount of loaded entries is
     * unlimited.
     * 
     * @param seriesId
     *            {@link UUID} of the series to load data for
     */
    public GetMiniOverallLeaderbordAction(UUID seriesId, String leaderboardName) {
        this(seriesId, leaderboardName, 0);
    }
    
    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given series-id, where the loaded
     * entries are limited to the provided amount.
     * 
     * @param seriesId
     *            {@link UUID} of the series to load data for
     * @param limit
     *            maximum number of entries to be loaded
     */
    public GetMiniOverallLeaderbordAction(UUID seriesId, String leaderboardName, int limit) {
        this.seriesId = seriesId;
        this.leaderboardName = leaderboardName;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(SailingDispatchContext context) {
        return EventActionUtil.getOverallLeaderboardContext(context, seriesId).calculateMiniLeaderboard(context.getRacingEventService(), limit);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(seriesId).append("_").append(limit);
    }

    @Override
    public String getLeaderboardName() {
        return leaderboardName;
    }
}
