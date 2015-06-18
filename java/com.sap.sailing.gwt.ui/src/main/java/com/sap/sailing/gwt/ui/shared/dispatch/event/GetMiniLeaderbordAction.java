package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ListResult;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.server.RacingEventService;

public class GetMiniLeaderbordAction implements Action<ResultWithTTL<ListResult<MiniLeaderboardItemDTO>>> {
    private static final Logger logger = Logger.getLogger(GetMiniLeaderbordAction.class.getName());

    @SuppressWarnings("unused")
    private UUID eventId;
    private String leaderboardName;

    @SuppressWarnings("unused")
    private GetMiniLeaderbordAction() {
    }

    public GetMiniLeaderbordAction(UUID eventId, String leaderboardName) {
        this.eventId = eventId;
        this.leaderboardName = leaderboardName;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<ListResult<MiniLeaderboardItemDTO>> execute(DispatchContext context) {
        final Leaderboard leaderboard = context.getRacingEventService().getLeaderboardByName(leaderboardName);
        if (leaderboard == null) {
            return new ResultWithTTL<ListResult<MiniLeaderboardItemDTO>>(1000 * 60 * 5, new ListResult<>(
                    Collections.<MiniLeaderboardItemDTO> emptyList()));
        }
        RacingEventService service = context.getRacingEventService();
        try {
            LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(null, Collections.<String> emptyList(), true,
                    service, service.getBaseDomainFactory(), false);
            int rank = 0;
            LinkedList<MiniLeaderboardItemDTO> items = new LinkedList<MiniLeaderboardItemDTO>();
            for (CompetitorDTO competitor : leaderboardDTO.competitors) {
                rank++;
                LeaderboardRowDTO row = leaderboardDTO.rows.get(competitor);
                items.add(new MiniLeaderboardItemDTO(competitor, rank, row.totalPoints));
            }
            return new ResultWithTTL<ListResult<MiniLeaderboardItemDTO>>(1000 * 60 * 5,
                    new ListResult<MiniLeaderboardItemDTO>(items));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading leaderboard", e);
            throw new DispatchException("Error loading leaderboard");
        }
    }
}
