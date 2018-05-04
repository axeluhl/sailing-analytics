package com.sap.sailing.gwt.home.communication.event.minileaderboard;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown in compact leaderboard (e.g. on the event overview
 * page) for the {@link #GetMiniLeaderbordAction(UUID, String) given event-id and leaderboard name}, where the amount of
 * loaded entries can optionally be {@link #GetMiniLeaderbordAction(UUID, String, int) limited}.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live is <i>1 minute</i> for currently running events, <i>2 minutes</i>
 * otherwise.
 * </p>
 */
public class GetMiniLeaderbordAction implements SailingAction<ResultWithTTL<GetMiniLeaderboardDTO>>, IsClientCacheable, ProvidesLeaderboardRouting {
    private UUID eventId;
    private String leaderboardName;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniLeaderbordAction() {
    }

    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given event-id and leaderboard name, where the amount
     * of loaded entries is unlimited.
     * 
     * @param eventId
     *            {@link UUID} of the {@link Event} to load data for
     * @param leaderboardName
     *            {@link String leaderboard name} to load data for
     */
    public GetMiniLeaderbordAction(UUID eventId, String leaderboardName) {
        this(eventId, leaderboardName, 0);
    }
    
    /**
     * Creates a {@link GetMiniLeaderbordAction} instance for the given event-id and leaderboard name, where the loaded
     * entries are limited to the provided amount.
     * 
     * @param eventId
     *            {@link UUID} of the {@link Event} to load data for
     * @param leaderboardName
     *            {@link String leaderboard name} to load data for
     * @param limit
     *            maximum number of entries to be loaded
     */
    public GetMiniLeaderbordAction(UUID eventId, String leaderboardName, int limit) {
        this.eventId = eventId;
        this.leaderboardName = leaderboardName;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(SailingDispatchContext context) {
        return EventActionUtil.getLeaderboardContext(context, eventId, leaderboardName).calculateMiniLeaderboard(context.getRacingEventService(), limit);
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId);
        key.append("_");
        key.append(leaderboardName);
        key.append("_");
        key.append(limit);
    }

    @Override
    public String getLeaderboardName() {
        return leaderboardName;
    }
}
