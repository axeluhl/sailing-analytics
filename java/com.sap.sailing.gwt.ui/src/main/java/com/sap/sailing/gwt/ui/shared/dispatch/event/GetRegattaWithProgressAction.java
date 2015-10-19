package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.home.shared.dispatch.IsClientCacheable;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sse.common.Duration;

public class GetRegattaWithProgressAction implements Action<ResultWithTTL<RegattaWithProgressDTO>>, IsClientCacheable {

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
    public ResultWithTTL<RegattaWithProgressDTO> execute(DispatchContext context) {
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, Duration.ONE_MINUTE.times(5)),
                EventActionUtil.getLeaderboardContext(context, eventId, regattaId).getRegattaWithProgress());
    }

    @Override
    public void cacheInstanceKey(StringBuilder key) {
        key.append(eventId).append("_").append(regattaId);
    }
}
