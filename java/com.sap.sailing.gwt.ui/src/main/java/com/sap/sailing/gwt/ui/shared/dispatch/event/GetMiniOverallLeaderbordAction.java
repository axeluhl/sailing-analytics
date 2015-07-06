package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Util;

public class GetMiniOverallLeaderbordAction implements Action<ResultWithTTL<GetMiniLeaderboardDTO>> {
    private static final Logger logger = Logger.getLogger(GetMiniOverallLeaderbordAction.class.getName());

    private UUID seriesId;
    private int limit = 0;

    @SuppressWarnings("unused")
    private GetMiniOverallLeaderbordAction() {
    }

    public GetMiniOverallLeaderbordAction(UUID eventId) {
        this(eventId, 0);
    }
    
    public GetMiniOverallLeaderbordAction(UUID seriesId, int limit) {
        this.seriesId = seriesId;
        this.limit = limit;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<GetMiniLeaderboardDTO> execute(DispatchContext context) {
        RacingEventService service = context.getRacingEventService();
        Event event = service.getEvent(seriesId);
        if(event == null || !HomeServiceUtil.isFakeSeries(event)) {
            throw new DispatchException("The event is not part of an event series");
        }
        
        LeaderboardGroup leaderboardGroup = Util.get(event.getLeaderboardGroups(), 0);
        Leaderboard leaderboard = leaderboardGroup.getOverallLeaderboard();
        
        GetMiniLeaderboardDTO result = new GetMiniLeaderboardDTO();
        if (leaderboard == null) {
            return new ResultWithTTL<>(1000 * 60 * 5, result);
        }
        
        try {
            LeaderboardDTO leaderboardDTO = leaderboard.getLeaderboardDTO(null, Collections.<String> emptyList(), true,
                    service, service.getBaseDomainFactory(), false);
            
            result.setScoreCorrectionText(leaderboardDTO.getComment());
            result.setLastScoreUpdate(leaderboardDTO.getTimePointOfLastCorrectionsValidity());
            boolean isLive = HomeServiceUtil.hasLiveRace(leaderboardDTO);
            result.setLive(isLive);
            
            int rank = 0;
            for (CompetitorDTO competitor : leaderboardDTO.competitors) {
                rank++;
                LeaderboardRowDTO row = leaderboardDTO.rows.get(competitor);
                result.addItem(new MiniLeaderboardItemDTO(new SimpleCompetitorDTO(competitor), rank, row.totalPoints));
                if (limit > 0 && rank >= limit) break;
            }
            int ttl = isLive ? 1000 * 60 : 1000 * 60 * 2;
            return new ResultWithTTL<>(ttl, result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading leaderboard", e);
            throw new DispatchException("Error loading leaderboard");
        }
    }
}
