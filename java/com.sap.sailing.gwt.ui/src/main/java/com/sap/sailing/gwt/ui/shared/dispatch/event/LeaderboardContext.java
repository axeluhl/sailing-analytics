package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collections;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.EventBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.LeaderboardEntryDTO;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.server.HomeServiceUtil;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchException;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.EventActionUtil.RaceCallback;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.ui.shared.eventview.HasRegattaMetadata.RegattaState;
import com.sap.sailing.gwt.ui.shared.eventview.RegattaMetadataDTO;
import com.sap.sailing.gwt.ui.shared.general.EventState;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class LeaderboardContext {
    private static final Logger logger = Logger.getLogger(LeaderboardContext.class.getName());

    private final TimePoint now = MillisecondsTimePoint.now();
    private final Event event;
    private final LeaderboardGroup leaderboardGroup;
    private final Leaderboard leaderboard;
    private final RacingEventService service;

    public LeaderboardContext(DispatchContext dispatchContext, Event event, LeaderboardGroup leaderboardGroup, Leaderboard leaderboard) {
        this.service = dispatchContext.getRacingEventService();
        this.event = event;
        this.leaderboardGroup = leaderboardGroup;
        this.leaderboard = leaderboard;
    }
    
    public void forRaces(RaceCallback callback) {
        for(RaceColumn raceColumn : leaderboard.getRaceColumns()) {
            if(!raceColumn.isCarryForward()) {
                for(Fleet fleet : raceColumn.getFleets()) {
                    callback.doForRace(new RaceContext(service, event, leaderboard, raceColumn, fleet, service));
                }
            }
        }
    }
    
    public RegattaWithProgressDTO getRegattaWithProgress() {
        RegattaProgressCalculator regattaProgressCalculator = new RegattaProgressCalculator();
        forRaces(regattaProgressCalculator);
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
            boolean isLive = hasLiveRace();
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
            return new ResultWithTTL<>(Duration.ONE_MINUTE.times(isLive ? 1 : 2), result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading leaderboard", e);
            throw new DispatchException("Error loading leaderboard");
        }
    }

    private boolean hasLiveRace() {
        OverallRacesStateCalculator racesStateCalculator = new OverallRacesStateCalculator();
        forRaces(racesStateCalculator);
        return racesStateCalculator.hasLiveRace();
    }
    
    public RegattaState calculateRegattaState() {
        // First using event state -> fast and ensures that all regattas are marked as finished after the event is finished
        EventState eventState = HomeServiceUtil.calculateEventState(event);
        if(eventState == EventState.FINISHED) {
            return RegattaState.FINISHED;
        }
        if(eventState == EventState.UPCOMING || eventState == EventState.PLANNED) {
            return RegattaState.UPCOMING;
        }
        
        // Using regatta start and end -> fast calculation of upcoming and finished states but not helpful to
        // distinguish between live and progress
        TimePoint startDate = getStartTimePoint();
        if(startDate != null && now.before(startDate)) {
            return RegattaState.UPCOMING;
        }
        TimePoint endDate = getEndTimePoint();
        if(endDate != null && now.after(endDate)) {
            return RegattaState.FINISHED;
        }
        
        // Using the race states to calculate the real state for running events/regattas
        OverallRacesStateCalculator racesStateCalculator = new OverallRacesStateCalculator();
        forRaces(racesStateCalculator);
        if(racesStateCalculator.hasLiveRace()) {
            return RegattaState.RUNNING;
        }
        if(!racesStateCalculator.hasUnfinishedRace()) {
            return RegattaState.FINISHED;
        }
        if(racesStateCalculator.hasAbandonedOrPostponedRace() || racesStateCalculator.hasFinishedRace()) {
            return RegattaState.PROGRESS;
        }
        return RegattaState.UPCOMING;
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
        regattaDTO.setRaceCount(HomeServiceUtil.calculateRaceCount(leaderboard));
        regattaDTO.setBoatClass(HomeServiceUtil.getBoatClassName(leaderboard));
        if(leaderboard instanceof RegattaLeaderboard) {
            regattaDTO.setStartDate(getStartDateWithEventFallback());
            regattaDTO.setEndDate(getEndDateWithEventFallback());
        }
        regattaDTO.setState(calculateRegattaState());
        regattaDTO.setDefaultCourseAreaName(HomeServiceUtil.getCourseAreaNameForRegattaIdThereIsMoreThanOne(event, leaderboard));
        regattaDTO.setDefaultCourseAreaId(HomeServiceUtil.getCourseAreaIdForRegatta(event, leaderboard));
        regattaDTO.setFlexibleLeaderboard(leaderboard instanceof FlexibleLeaderboard);
    }
    
    private static boolean hasMultipleLeaderboardGroups(EventBase event) {
        return Util.size(event.getLeaderboardGroups()) > 1;
    }

    public boolean isPartOfEvent() {
        return HomeServiceUtil.isPartOfEvent(event, leaderboard);
    }
    
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
}
