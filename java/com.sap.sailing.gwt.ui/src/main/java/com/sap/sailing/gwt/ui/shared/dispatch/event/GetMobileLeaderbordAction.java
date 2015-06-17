package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.LinkedList;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMobileLeaderbordAction.SimplifiedLeaderboardItemDTO;

public class GetMobileLeaderbordAction implements Action<ListResult<SimplifiedLeaderboardItemDTO>> {
    private UUID eventId;
    private String leaderboardName;

    @SuppressWarnings("unused")
    private GetMobileLeaderbordAction() {
    }

    public GetMobileLeaderbordAction(UUID eventId, String leaderboardName) {
        this.eventId = eventId;
        this.leaderboardName = leaderboardName;
    }

    @Override
    @GwtIncompatible
    public ListResult<SimplifiedLeaderboardItemDTO> execute(DispatchContext context) {
        LinkedList<SimplifiedLeaderboardItemDTO> items = new LinkedList<SimplifiedLeaderboardItemDTO>();
        // TODO: mini leaderboard magic
        return new ListResult<SimplifiedLeaderboardItemDTO>(items);
    }

    public static class SimplifiedLeaderboardItemDTO implements DTO {
        
    }

}
