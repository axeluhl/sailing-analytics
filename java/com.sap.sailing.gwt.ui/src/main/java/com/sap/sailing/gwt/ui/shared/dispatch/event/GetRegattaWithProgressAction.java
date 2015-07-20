package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;

public class GetRegattaWithProgressAction implements Action<ResultWithTTL<RegattaWithProgressDTO>> {

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
        return new ResultWithTTL<>(EventActionUtil.getEventStateDependentTTL(context, eventId, 5 * 60 * 1000),
                EventActionUtil.getLeaderboardContext(context, eventId, regattaId).getRegattaWithProgress(context));
    }
}
