package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.text.MessageFormat;
import java.util.Collections;
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
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.server.RacingEventService;

public class GetMiniLeaderbordAction implements Action<ResultWithTTL<GetMiniLeaderboardDTO>> {
    private static final Logger logger = Logger.getLogger(GetMiniLeaderbordAction.class.getName());
    private static final String urlTemplate = "Leaderboard.html?name={0}&displayName={1}&embedded=true&hideToolbar=true&refreshIntervalMillis=3000&legDetail=AVERAGE_SPEED_OVER_GROUND_IN_KNOTS&legDetail=DISTANCE_TRAVELED&legDetail=RANK_GAIN&overallDetail=REGATTA_RANK&maneuverDetail=TACK&maneuverDetail=JIBE&maneuverDetail=PENALTY_CIRCLE&lastN=1&showAddedScores=false";

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
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(DispatchContext context) {
        RacingEventService service = context.getRacingEventService();
        final Leaderboard leaderboard = service.getLeaderboardByName(leaderboardName);
        GetMiniLeaderboardDTO result = new GetMiniLeaderboardDTO();
        if (leaderboard == null) {
            return new ResultWithTTL<>(1000 * 60 * 5, result);
        }
        result.setLeaderboardDetailsURL(MessageFormat.format(urlTemplate, leaderboardName, leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName()));
        
        try {
            LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(null, Collections.<String> emptyList(), true,
                    service, service.getBaseDomainFactory(), false);
            
            result.setScoreCorrectionText(leaderboardDTO.getComment());
            result.setLastScoreUpdate(leaderboardDTO.getTimePointOfLastCorrectionsValidity());
            
            int rank = 0;
            for (CompetitorDTO competitor : leaderboardDTO.competitors) {
                rank++;
                LeaderboardRowDTO row = leaderboardDTO.rows.get(competitor);
                result.addItem(new MiniLeaderboardItemDTO(competitor, rank, row.totalPoints));
            }
            return new ResultWithTTL<>(1000 * 60 * 5, result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading leaderboard", e);
            throw new DispatchException("Error loading leaderboard");
        }
    }
}
