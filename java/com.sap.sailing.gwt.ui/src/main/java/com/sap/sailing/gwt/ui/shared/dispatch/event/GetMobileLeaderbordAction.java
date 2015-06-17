package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.event.GetMobileLeaderbordAction.SimplifiedLeaderboardItemDTO;
import com.sap.sailing.server.RacingEventService;

public class GetMobileLeaderbordAction implements Action<ListResult<SimplifiedLeaderboardItemDTO>> {
    private static final Logger logger = Logger.getLogger(GetMobileLeaderbordAction.class.getName());
    
    @SuppressWarnings("unused")
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
        final Leaderboard leaderboard = context.getRacingEventService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return new ListResult<>(Collections.<SimplifiedLeaderboardItemDTO>emptyList());
        }
        RacingEventService service = context.getRacingEventService();
        try {
            LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(null,
                    Collections.<String>emptyList(), true, service, service.getBaseDomainFactory(), false);
            
            // TODO: mini leaderboard magic
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading leaderboard", e);
            throw new DispatchException("Error loading leaderboard");
        }
        
        LinkedList<SimplifiedLeaderboardItemDTO> items = new LinkedList<SimplifiedLeaderboardItemDTO>();
        return new ListResult<SimplifiedLeaderboardItemDTO>(items);
    }

    public static class SimplifiedLeaderboardItemDTO implements DTO {
        
    }

}
