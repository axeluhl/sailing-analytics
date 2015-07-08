package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.server.RacingEventService;

public class LeaderboardContext {
    private static final Logger logger = Logger.getLogger(LeaderboardContext.class.getName());

    private final Event event;
    private final LeaderboardGroup leaderboardGroup;
    private final Leaderboard leaderboard;

    public LeaderboardContext(Event event, LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        this.event = event;
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
    }
    
    public void forRaces(DispatchContext context, RaceCallback callback) {
        for(RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for(Fleet fleet : raceColumn.getFleets()) {
                callback.doForRace(new RaceContext(event, leaderboard, raceColumn, fleet, context.getRacingEventService()));
            }
        }
    }
    
    public RegattaWithProgressDTO getRegattaWithProgress(DispatchContext context) {
        RegattaProgressCalculator regattaProgressCalculator = new RegattaProgressCalculator();
        forRaces(context, regattaProgressCalculator);
        RegattaWithProgressDTO regattaDTO = new RegattaWithProgressDTO(regattaProgressCalculator.getResult());
        HomeServiceUtil.fillRegattaFields(leaderboardGroup, leaderboard, regattaDTO);
        return regattaDTO;
    }
    
    public ResultWithTTL<GetMiniLeaderboardDTO> calculateMiniLeaderboard(RacingEventService service, int limit) {
        GetMiniLeaderboardDTO result = new GetMiniLeaderboardDTO();
        if (leaderboard == null) {
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
                int raceCount = row.fieldsByRaceColumnName == null ? 0 : row.fieldsByRaceColumnName.size();
                result.addItem(new MiniLeaderboardItemDTO(new SimpleCompetitorDTO(competitor), rank, row.totalPoints, raceCount));
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
