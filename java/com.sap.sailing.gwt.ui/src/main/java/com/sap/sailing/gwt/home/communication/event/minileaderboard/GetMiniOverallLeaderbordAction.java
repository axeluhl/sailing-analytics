package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.LeaderboardContext;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in compact leaderboard (e.g. on the series overview
 * page) for the {@link #GetMiniOverallLeaderbordAction(UUID) given leaderboardGroupUUID}, where the amount of loaded entries can
 * optionally be {@link #GetMiniOverallLeaderbordAction(UUID, int) limited}.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>1 minute</i> for currently running events, <i>2 minutes</i>
 * otherwise.
 * </p>
 */
public class GetMiniOverallLeaderbordAction implements SailingAction<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable, ProvidesLeaderboardRouting {
    private UUID leaderboardGroupUUID;
    private String leaderboardName;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniOverallLeaderbordAction() {
    }

    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given series-id, where the amount of loaded entries is
     * unlimited.
     * 
     * @param leaderboardGroupUUID
     *            {@link UUID} of the series to load data for
     */
    public GetMiniOverallLeaderbordAction(UUID leaderboardGroupUUID, String leaderboardName) {
        this(leaderboardGroupUUID, leaderboardName, 0);
    }
    
    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given series-id, where the loaded
     * entries are limited to the provided amount.
     * 
     * @param leaderboardGroupUUID
     *            {@link UUID} of the series to load data for
     * @param limit
     *            maximum number of entries to be loaded
     */
    public GetMiniOverallLeaderbordAction(UUID leaderboardGroupUUID, String leaderboardName, int limit) {
        this.leaderboardGroupUUID = leaderboardGroupUUID;
        this.leaderboardName = leaderboardName;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(SailingDispatchContext context) {
        LeaderboardGroup leaderboardGroup = context.getRacingEventService().getLeaderboardGroupByID(leaderboardGroupUUID);
        if(leaderboardGroup == null) {
            throw new RuntimeException("Invalid leaderboardGroupID");
        }
        Event event = HomeServiceUtil.determineBestMatchingEvent(context.getRacingEventService(), leaderboardGroup);
        LeaderboardContext lctx = new LeaderboardContext(context, event, event.getLeaderboardGroups(), leaderboardGroup.getOverallLeaderboard());
        return lctx.calculateMiniLeaderboard(context.getRacingEventService(), limit);
        
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(leaderboardGroupUUID).append("_").append(limit);
    }

    @Override
    public String getLeaderboardName() {
        return leaderboardName;
    }
}
