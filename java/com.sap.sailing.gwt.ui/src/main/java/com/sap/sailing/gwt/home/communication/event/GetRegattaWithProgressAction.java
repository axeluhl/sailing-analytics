package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.common.communication.routing.ProvidesLeaderboardRouting;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sailing.gwt.home.server.RaceCompetitionFormatDataCalculator;
import com.sap.sse.common.Duration;
import com.sap.sse.gwt.dispatch.shared.caching.IsClientCacheable;
import com.sap.sse.gwt.dispatch.shared.commands.ResultWithTTL;

/**
 * <p>
 * {@link SailingAction} implementation to load data to be shown on the regatta overview page for the
 * {@link #GetRegattaWithProgressAction(UUID, String) given event- and regatta-id}, using a
 * {@link RaceCompetitionFormatDataCalculator} to prepare the appropriate data structure.
 * </p>
 * <p>
 * The {@link ResultWithTTL result's} time to live
 * {@link EventActionUtil#getEventStateDependentTTL(SailingDispatchContext, UUID, Duration) depends on the event's
 * state} using a duration of <i>5 minutes</i> for currently running events.
 * </p>
 */
public class GetRegattaWithProgressAction implements SailingAction<ResultWithTTL<RegattaWithProgressDTO>>, IsClientCacheable, ProvidesLeaderboardRouting {

    private UUID eventId;
    private String regattaId;

    @SuppressWarnings("unused")
    private GetRegattaWithProgressAction() {
    }

    /**
     * Creates a {@link GetRegattaWithProgressAction} instance for the given event and regatta-id.
     * 
     * @param eventId {@link UUID} of the event to load races for
     * @param regattaId {@link String id} of the regatta to load races for
     */
    public GetRegattaWithProgressAction(UUID eventId, String regattaId) {
        this.eventId = eventId;
        this.regattaId = regattaId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<RegattaWithProgressDTO> execute(SailingDispatchContext context) {
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, Duration.ONE_MINUTE.times(5)),
                EventActionUtil.getLeaderboardContext(context, eventId, regattaId).getRegattaWithProgress());
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
