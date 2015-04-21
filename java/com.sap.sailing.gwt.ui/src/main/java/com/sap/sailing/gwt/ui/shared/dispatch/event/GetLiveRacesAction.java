package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnInSeries;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class GetLiveRacesAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    
    private UUID eventId;
    
    public GetLiveRacesAction() {
    }

    public GetLiveRacesAction(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getEventId() {
        return eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        MillisecondsTimePoint now = MillisecondsTimePoint.now();
        LiveRacesDTO result = new LiveRacesDTO();

        Event event = context.getRacingEventService().getEvent(getEventId());
        for (LeaderboardGroup lg : event.getLeaderboardGroups()) {
            for (Leaderboard lb : lg.getLeaderboards()) {
                String regattaName = null;
                if(lb instanceof RegattaLeaderboard) {
                    regattaName = ((RegattaLeaderboard) lb).getRegatta().getName();
                }
                for(RaceColumn raceColumn : lb.getRaceColumns()) {
                    if(raceColumn instanceof RaceColumnInSeries) {
                        Series series = ((RaceColumnInSeries) raceColumn).getSeries();
                    }
                    for(Fleet fleet : raceColumn.getFleets()) {
                        final RaceLog raceLog = raceColumn.getRaceLog(fleet);
                        if(raceLog == null) {
                            // No racelog -> we can't decide if the race is live
                            continue;
                        }
                        final ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
                        if(state.getStatus().compareTo(RaceLogRaceStatus.STARTPHASE) < 0 || state.getStatus().compareTo(RaceLogRaceStatus.FINISHING) > 0) {
                            // race isn't live
                            continue;
                        }
                        
                        LiveRaceDTO liveRaceDTO = new LiveRaceDTO();
                        liveRaceDTO.setRegattaName(regattaName);
                        liveRaceDTO.setFleetName(fleet.getName());
                        liveRaceDTO.setFleetColor(fleet.getColor().getAsHtml());
                        liveRaceDTO.setRaceName(raceColumn.getName());
                        
                        liveRaceDTO.setStart(state.getStartTime().asDate());
                        
                        result.addRace(liveRaceDTO);
                    }
                }
//                // Regatta regatta = getService().getRegattaByName(lb.getName());
//                for (TrackedRace trackedRace : lb.getTrackedRaces()) {
//                    // trackedRace.getMarks()
//                    if (trackedRace.getStartOfRace().before(now) && trackedRace.getEndOfRace().after(now)) {
//                        result.addRace(new LiveRaceDTO(trackedRace.getRace().getName()));
//                    }
//                }
                // for (RaceDefinition rd : regatta.getAllRaces()) {
                // rd.
                // }
            }
        }
        
//        Calendar dayToCheck = Calendar.getInstance();
//        dayToCheck.setTime(new Date());
//        
//        Event event = context.getRacingEventService().getEvent(eventId);
//        if (event != null) {
//            for (CourseArea courseArea : event.getVenue().getCourseAreas()) {
//                for (Leaderboard leaderboard : context.getRacingEventService().getLeaderboards().values()) {
//                    final CourseArea leaderboardDefaultCourseArea = leaderboard.getDefaultCourseArea();
//                    if (leaderboardDefaultCourseArea != null && leaderboardDefaultCourseArea.equals(courseArea)) {
//                        result.addAll(getRaceStateEntriesForLeaderboard(leaderboard.getName(),
//                                true, true, visibleRegattas));
//                    }
//                }
//            }
//        }
        
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }
    
//    private List<RegattaOverviewEntryDTO> getRaceStateEntriesForLeaderboard(Leaderboard leaderboard,
//            boolean showOnlyCurrentlyRunningRaces, boolean showOnlyRacesOfSameDay, final List<String> visibleRegattas)
//            throws NoWindException, InterruptedException, ExecutionException {
//        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
//        Calendar dayToCheck = Calendar.getInstance();
//        dayToCheck.setTime(new Date());
//        CourseArea usedCourseArea = leaderboard.getDefaultCourseArea();
//        if (leaderboard != null) {
//            if (visibleRegattas != null && !visibleRegattas.contains(leaderboard.getName())) {
//                return result;
//            }
//            String regattaName = getRegattaNameFromLeaderboard(leaderboard);
//            if (leaderboard instanceof RegattaLeaderboard) {
//                RegattaLeaderboard regattaLeaderboard = (RegattaLeaderboard) leaderboard;
//                for (Series series : regattaLeaderboard.getRegatta().getSeries()) {
//                    Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
//                    for (RaceColumn raceColumn : series.getRaceColumns()) {
//                        getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck,
//                                usedCourseArea, leaderboard, regattaName, series.getName(), raceColumn, entriesPerFleet);
//                    }
//                    result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
//                }
//
//            } else {
//                Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet = new HashMap<String, List<RegattaOverviewEntryDTO>>();
//                for (RaceColumn raceColumn : leaderboard.getRaceColumns()) {
//                    getRegattaOverviewEntries(showOnlyRacesOfSameDay, dayToCheck, usedCourseArea,
//                            leaderboard, regattaName, LeaderboardNameConstants.DEFAULT_SERIES_NAME, raceColumn, entriesPerFleet);
//                }
//                result.addAll(getRegattaOverviewEntriesToBeShown(showOnlyCurrentlyRunningRaces, entriesPerFleet));
//            }
//        }
//        return result;
//    }
//    
//    private List<RegattaOverviewEntryDTO> getRegattaOverviewEntriesToBeShown(boolean showOnlyCurrentlyRunningRaces,
//            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {
//        List<RegattaOverviewEntryDTO> result = new ArrayList<RegattaOverviewEntryDTO>();
//        for (List<RegattaOverviewEntryDTO> entryList : entriesPerFleet.values()) {
//            result.addAll(entryList);
//            if (showOnlyCurrentlyRunningRaces) {
//                List<RegattaOverviewEntryDTO> finishedEntries = new ArrayList<RegattaOverviewEntryDTO>();
//                for (RegattaOverviewEntryDTO entry : entryList) {
//                    if (!RaceLogRaceStatus.isActive(entry.raceInfo.lastStatus)) {
//                        if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
//                            finishedEntries.add(entry);
//                        } else if (entry.raceInfo.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
//                            //don't filter when the race is unscheduled and aborted before
//                            if (!entry.raceInfo.isRaceAbortedInPassBefore) {
//                                result.remove(entry);
//                            }
//                            
//                        }
//                    }
//                }
//                if (!finishedEntries.isEmpty()) {
//                    //keep the last finished race in the list to be shown
//                    int indexOfLastElement = finishedEntries.size() - 1;
//                    finishedEntries.remove(indexOfLastElement);
//                    
//                    //... and remove all other finished races
//                    result.removeAll(finishedEntries);
//                }
//            }
//        }
//        return result;
//    }
//    
//    private void getRegattaOverviewEntries(boolean showOnlyRacesOfSameDay, Calendar dayToCheck,
//            CourseArea courseArea, Leaderboard leaderboard, String regattaName, String seriesName, RaceColumn raceColumn,
//            Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet) {
//
//        for (Fleet fleet : raceColumn.getFleets()) {
//            RegattaOverviewEntryDTO entry = createRegattaOverviewEntryDTO(courseArea,
//                    leaderboard, regattaName, seriesName, raceColumn, fleet, 
//                    showOnlyRacesOfSameDay, dayToCheck);
//            if (entry != null) {
//                addRegattaOverviewEntryToEntriesPerFleet(entriesPerFleet, fleet, entry);
//            }
//        }
//    }
//    
//    private void addRegattaOverviewEntryToEntriesPerFleet(Map<String, List<RegattaOverviewEntryDTO>> entriesPerFleet,
//            Fleet fleet, RegattaOverviewEntryDTO entry) {
//        if (!entriesPerFleet.containsKey(fleet.getName())) {
//           entriesPerFleet.put(fleet.getName(), new ArrayList<RegattaOverviewEntryDTO>()); 
//        }
//        entriesPerFleet.get(fleet.getName()).add(entry);
//    }
//    
//    private RegattaOverviewEntryDTO createRegattaOverviewEntryDTO(CourseArea courseArea, Leaderboard leaderboard,
//            String regattaName, String seriesName, RaceColumn raceColumn, Fleet fleet, boolean showOnlyRacesOfSameDay, Calendar dayToCheck) {
//        RegattaOverviewEntryDTO entry = new RegattaOverviewEntryDTO();
//        if (courseArea != null) {
//            entry.courseAreaName = courseArea.getName();
//            entry.courseAreaIdAsString = courseArea.getId().toString();
//        } else {
//            entry.courseAreaName = "Default";
//            entry.courseAreaIdAsString = "Default";
//        }
//        entry.regattaDisplayName = regattaName;
//        entry.regattaName = leaderboard.getName();
//        entry.raceInfo = createRaceInfoDTO(seriesName, raceColumn, fleet);
//        entry.currentServerTime = new Date();
//        
//        if (showOnlyRacesOfSameDay) {
//            if (!RaceStateOfSameDayHelper.isRaceStateOfSameDay(entry.raceInfo.startTime, entry.raceInfo.finishedTime, entry.raceInfo.abortingTimeInPassBefore, dayToCheck)) {
//                entry = null;
//            }
//        }
//        return entry;
//    }
//    
//
//    private RaceInfoDTO createRaceInfoDTO(String seriesName, RaceColumn raceColumn, Fleet fleet) {
//        RaceInfoDTO raceInfoDTO = new RaceInfoDTO();
//        RaceLog raceLog = raceColumn.getRaceLog(fleet);
//        if (raceLog != null) {
//            
//            raceInfoDTO.isTracked = raceColumn.getTrackedRace(fleet) != null ? true : false;
//            ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
//            
//            TimePoint startTime = state.getStartTime();
//            if (startTime != null) {
//                raceInfoDTO.startTime = startTime.asDate();
//            }
//
//            raceInfoDTO.lastStatus = state.getStatus();
//            
//            if (raceLog.getLastRawFix() != null) {
//                raceInfoDTO.lastUpdateTime = raceLog.getLastRawFix().getCreatedAt().asDate();
//            }
//            
//            TimePoint finishedTime = state.getFinishedTime();
//            if (finishedTime != null) {
//                raceInfoDTO.finishedTime = finishedTime.asDate();
//            } else {
//                raceInfoDTO.finishedTime = null;
//                if (raceInfoDTO.isTracked) {
//                    TimePoint endOfRace = raceColumn.getTrackedRace(fleet).getEndOfRace();
//                    raceInfoDTO.finishedTime = endOfRace != null ? endOfRace.asDate() : null;
//                }
//            }
//
//            if (startTime != null) {
//                FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, MillisecondsTimePoint.now());
//                List<FlagPole> activeFlags = activeFlagState.getCurrentState();
//                FlagPoleState previousFlagState = activeFlagState.getPreviousState(state.getRacingProcedure(), startTime);
//                List<FlagPole> previousFlags = previousFlagState.getCurrentState();
//                FlagPole mostInterestingFlagPole = FlagPoleState.getMostInterestingFlagPole(previousFlags, activeFlags);
//
//                // TODO: adapt the LastFlagFinder#getMostRecent method!
//                if (mostInterestingFlagPole != null) {
//                    raceInfoDTO.lastUpperFlag = mostInterestingFlagPole.getUpperFlag();
//                    raceInfoDTO.lastLowerFlag = mostInterestingFlagPole.getLowerFlag();
//                    raceInfoDTO.lastFlagsAreDisplayed = mostInterestingFlagPole.isDisplayed();
//                    raceInfoDTO.lastFlagsDisplayedStateChanged = previousFlagState.hasPoleChanged(mostInterestingFlagPole);
//                }
//            }
//            
//            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
//            
//            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
//            if (abortingFlagEvent != null) {
//                raceInfoDTO.isRaceAbortedInPassBefore = true;
//                raceInfoDTO.abortingTimeInPassBefore = abortingFlagEvent.getLogicalTimePoint().asDate();
//                
//                if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
//                    raceInfoDTO.lastUpperFlag = abortingFlagEvent.getUpperFlag();
//                    raceInfoDTO.lastLowerFlag = abortingFlagEvent.getLowerFlag();
//                    raceInfoDTO.lastFlagsAreDisplayed = abortingFlagEvent.isDisplayed();
//                    raceInfoDTO.lastFlagsDisplayedStateChanged = true;
//                }
//            }
//            
//            CourseBase lastCourse = state.getCourseDesign();
//            if (lastCourse != null) {
//                raceInfoDTO.lastCourseDesign = convertCourseDesignToRaceCourseDTO(lastCourse);
//                raceInfoDTO.lastCourseName = lastCourse.getName();
//            }
//            
//            if (raceInfoDTO.lastStatus.equals(RaceLogRaceStatus.FINISHED)) {
//                TimePoint protestStartTime = state.getProtestTime();
//                if (protestStartTime != null) {
//                    long protestDuration = 90 * 60 * 1000; // 90 min protest duration
//                    raceInfoDTO.protestFinishTime = protestStartTime.plus(protestDuration).asDate();
//                    raceInfoDTO.lastUpperFlag = Flags.BRAVO;
//                    raceInfoDTO.lastLowerFlag = Flags.NONE;
//                    raceInfoDTO.lastFlagsAreDisplayed = true;
//                    raceInfoDTO.lastFlagsDisplayedStateChanged = true;
//                }
//            }
//            
//            Wind wind = state.getWindFix();
//            if (wind != null) {
//                raceInfoDTO.lastWind = createWindDTOFromAlreadyAveraged(wind, MillisecondsTimePoint.now());
//            }
//
//            fillStartProcedureSpecifics(raceInfoDTO, state);
//        }
//        raceInfoDTO.seriesName = seriesName;
//        raceInfoDTO.raceName = raceColumn.getName();
//        raceInfoDTO.fleetName = fleet.getName();
//        raceInfoDTO.fleetOrdering = fleet.getOrdering();
//        raceInfoDTO.raceIdentifier = raceColumn.getRaceIdentifier(fleet);
//        raceInfoDTO.isTracked = raceColumn.getTrackedRace(fleet) != null ? true : false;
//        return raceInfoDTO;
//    }
}
