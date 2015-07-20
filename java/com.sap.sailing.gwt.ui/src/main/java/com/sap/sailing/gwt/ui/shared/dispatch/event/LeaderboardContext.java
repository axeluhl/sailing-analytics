package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;

public class LeaderboardContext {
    private static final Logger logger = Logger.getLogger(LeaderboardContext.class.getName());

    private final Event event;
    private final LeaderboardGroup leaderboardGroup;
    private final Leaderboard leaderboard;
    private final RacingEventService service;

    public LeaderboardContext(RacingEventService service, Event event, LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        this.service = service;
        this.event = event;
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
    }
    
    public void forRaces(DispatchContext context, RaceCallback callback) {
        for(RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            for(Fleet fleet : raceColumn.getFleets()) {
                callback.doForRace(new RaceContext(service, event, leaderboard, raceColumn, fleet, context.getRacingEventService()));
            }
        }
    }
    
    public RegattaWithProgressDTO getRegattaWithProgress(DispatchContext context) {
        RegattaProgressCalculator regattaProgressCalculator = new RegattaProgressCalculator();
        forRaces(context, regattaProgressCalculator);
        RegattaWithProgressDTO regattaDTO = new RegattaWithProgressDTO(regattaProgressCalculator.getResult());
        fillRegattaFields(regattaDTO);
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
            boolean isLive = hasLiveRace(leaderboardDTO);
            result.setLive(isLive);
            
            int rank = 0;
            for (CompetitorDTO competitor : leaderboardDTO.competitors) {
                rank++;
                LeaderboardRowDTO row = leaderboardDTO.rows.get(competitor);
                int raceCount = 0;
                if(row.fieldsByRaceColumnName != null) {
                    for(LeaderboardEntryDTO lbEntry : row.fieldsByRaceColumnName.values()) {
                        if(lbEntry.totalPoints != null) {
                            raceCount++;
                        }
                    }
                }
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

    private boolean hasLiveRace(LeaderboardDTO leaderboard) {
        List<Pair<RaceColumnDTO, FleetDTO>> liveRaces = leaderboard.getLiveRaces(HomeServiceUtil.getLiveTimePointInMillis());
        return !liveRaces.isEmpty();
    }
    
    private Date getStartDateWithEventFallback() {
        Date result = getStartDate();
        if(result != null) {
            return result;
        }
        return event.getStartDate().asDate();
    }
    
    private Date getStartDate() {
        TimePoint timePoint = getStartTimePoint();
        if(timePoint != null) {
            return timePoint.asDate();
        }
        return null;
    }
    
    private TimePoint getStartTimePoint() {
        if(leaderboard instanceof RegattaLeaderboard) {
            Regatta regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
            return regatta.getStartDate();
        }
        return null;
    }
    
    private Date getEndDateWithEventFallback() {
        Date result = getEndDate();
        if(result != null) {
            return result;
        }
        return event.getEndDate().asDate();
    }
    
    private Date getEndDate() {
        TimePoint timePoint = getEndTimePoint();
        if(timePoint != null) {
            return timePoint.asDate();
        }
        return null;
    }
    
    private TimePoint getEndTimePoint() {
        if(leaderboard instanceof RegattaLeaderboard) {
            Regatta regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
            return regatta.getEndDate();
        }
        return null;
    }

    public String getLeaderboardName() {
        return leaderboard.getName();
    }

    public RegattaMetadataDTO asRegattaMetadataDTO() {
        RegattaMetadataDTO regattaDTO = new RegattaMetadataDTO();
        fillRegattaFields(regattaDTO);
        
        return regattaDTO;
    }

    public void fillRegattaFields(RegattaMetadataDTO regattaDTO) {
        regattaDTO.setId(getLeaderboardName());
        regattaDTO.setDisplayName(leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : leaderboard.getName());
        if(hasMultipleLeaderboardGroups(event)) {
            regattaDTO.setBoatCategory(leaderboardGroup.getDisplayName() != null ? leaderboardGroup.getDisplayName() : leaderboardGroup.getName());
        }
        regattaDTO.setCompetitorsCount(HomeServiceUtil.calculateCompetitorsCount(leaderboard));
        regattaDTO.setRaceCount(HomeServiceUtil.calculateRaceColumnCount(leaderboard));
        regattaDTO.setBoatClass(HomeServiceUtil.getBoatClassName(leaderboard));
        if(leaderboard instanceof RegattaLeaderboard) {
            regattaDTO.setStartDate(getStartDateWithEventFallback());
            regattaDTO.setEndDate(getEndDateWithEventFallback());
        }
        regattaDTO.setState(HomeServiceUtil.calculateRegattaState(regattaDTO));
        regattaDTO.setDefaultCourseAreaName(HomeServiceUtil.getCourseAreaNameForRegattaIdThereIsMoreThanOne(event, leaderboard));
    }
    
    private static boolean hasMultipleLeaderboardGroups(EventBase event) {
        return Util.size(event.getLeaderboardGroups()) > 1;
    }

    public boolean isPartOfEvent() {
        return HomeServiceUtil.isPartOfEvent(event, leaderboard);
    }
}
