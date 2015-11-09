package com.sap.sailing.gwt.home.communication.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.dispatch.client.ResultWithTTL;
import com.sap.sailing.gwt.dispatch.client.caching.IsClientCacheable;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.home.server.EventActionUtil;
import com.sap.sse.common.Duration;

public class GetRegattaWithProgressAction implements SailingAction<ResultWithTTL<RegattaWithProgressDTO>>, IsClientCacheable {

    private UUID eventId;
    private String regattaId;

    @SuppressWarnings("unused")
    private GetRegattaWithProgressAction() {
    }

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
}
