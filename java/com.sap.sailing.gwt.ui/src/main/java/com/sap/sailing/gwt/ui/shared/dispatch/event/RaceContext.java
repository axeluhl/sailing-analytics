package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.AbortingFlagFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.WindFixesFinder;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TimingConstants;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.leaderboard.FlexibleLeaderboard;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceTrackingState;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
public class RaceContext {
    private final MillisecondsTimePoint now = MillisecondsTimePoint.now();
    private final Leaderboard leaderboard;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final RaceDefinition raceDefinition;
    private final TrackedRace trackedRace;
    private final RaceLog raceLog;
    private final ReadonlyRaceState state;
    private final Event event;
    private final long TIME_TO_SHOW_CANCELED_RACES_AS_LIVE = 5 * 60 * 1000; // 5 min
    
    public RaceContext(Event event, Leaderboard leaderboard, RaceColumn raceColumn, Fleet fleet) {
        this.event = event;
        this.leaderboard = leaderboard;
        this.raceColumn = raceColumn;
        this.raceDefinition = raceColumn.getRaceDefinition(fleet);
        this.fleet = fleet;
        trackedRace = raceColumn.getTrackedRace(fleet);
        raceLog = raceColumn.getRaceLog(fleet);
        state = ReadonlyRaceStateImpl.create(raceLog);
    }

    private boolean isShowFleetData() {
        return !LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleet.getName());
    }

    private String getRegattaName() {
        if (leaderboard instanceof RegattaLeaderboard) {
            Regatta regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
            return regatta.getName();
        }
        return leaderboard.getName();
    }

    private String getRegattaDisplayName() {
        String displayName = leaderboard.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        return leaderboard.getName();
    }

    private FleetMetadataDTO getFleetMetadataOrNull() {
        if (!isShowFleetData()) {
            return null;
        }
        return new FleetMetadataDTO(fleet.getName(), fleet.getColor() == null ? null : fleet.getColor().getAsHtml());
    }

    private SimpleWindDTO getWindOrNull() {
        if(trackedRace != null) {
            TimePoint toTimePoint = trackedRace.getEndOfRace() == null ? MillisecondsTimePoint.now().minus(trackedRace.getDelayToLiveInMillis()) : trackedRace.getEndOfRace();
            TimePoint newestEvent = trackedRace.getTimePointOfNewestEvent();
            if(newestEvent != null && newestEvent.before(toTimePoint)) {
                toTimePoint = newestEvent;
            }
            
            WindTrack windTrack = trackedRace.getOrCreateWindTrack(new WindSourceImpl(WindSourceType.COMBINED));
            
            if(windTrack != null) {
                WindWithConfidence<com.sap.sse.common.Util.Pair<Position, TimePoint>> averagedWindWithConfidence =
                        windTrack.getAveragedWindWithConfidence(trackedRace.getCenterOfCourse(toTimePoint), toTimePoint);
                if(averagedWindWithConfidence != null) {
                    Wind wind = averagedWindWithConfidence.getObject();
                    if (wind.getKnots() >= 0.05d) {
                        return new SimpleWindDTO(wind.getFrom().getDegrees(), wind.getKnots());
                    }
                }
            }
        } else {
            Wind wind = checkForWindFixesFromRaceLog();
            if(wind != null) {
                return new SimpleWindDTO(wind.getFrom().getDegrees(), wind.getKnots());
            }
        }
        return null;
    }

    private FlagStateDTO getFlagStateOrNull() {
        TimePoint startTime = state.getStartTime();
        if(startTime == null) {
            return null;
        }
        
        FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, now);
        List<FlagPole> activeFlags = activeFlagState.getCurrentState();
        FlagPoleState previousFlagState = activeFlagState.getPreviousState(state.getRacingProcedure(), startTime);
        List<FlagPole> previousFlags = previousFlagState.getCurrentState();
        FlagPole mostInterestingFlagPole = FlagPoleState.getMostInterestingFlagPole(previousFlags, activeFlags);

        // TODO: adapt the LastFlagFinder#getMostRecent method!
        if (mostInterestingFlagPole != null) {
            return new FlagStateDTO(mostInterestingFlagPole, previousFlagState);
        }
        return null;
    }
    
    private Regatta getRegatta() {
        final Regatta regatta;
        if (leaderboard instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) leaderboard).getRegatta();
        } else {
            regatta = null;
        }
        return regatta;
    }
    
    private String getBoatClassName() {
        BoatClass boatClass = getBoatClass();
        return boatClass == null ? null : boatClass.getName();
    }

    private BoatClass getBoatClass() {
        final BoatClass boatClass;
        if (getRegatta() != null) {
            boatClass = getRegatta().getBoatClass();
        } else {
            boatClass = getBoatClassFromTrackedRaces();
        }
        return boatClass;
    }

    private BoatClass getBoatClassFromTrackedRaces() {
        for (TrackedRace trackedRace : leaderboard.getTrackedRaces()) {
            return trackedRace.getRace().getBoatClass();
        }
        return null;
    }

    private String getCourseAreaOrNull() {
        /** The course area will not be shown if there is only one course area defined for the event */
        if(Util.size(event.getVenue().getCourseAreas()) <= 1) {
            return null;
        }
        CourseArea courseArea = null;
        if(leaderboard instanceof FlexibleLeaderboard) {
            courseArea = ((FlexibleLeaderboard)leaderboard).getDefaultCourseArea();
        }
        Regatta regatta = getRegatta();
        if(regatta != null) {
            courseArea = regatta.getDefaultCourseArea();
        }
        return courseArea == null ? null : regatta.getDefaultCourseArea().getName();
    }

    private RaceProgressDTO getProgressOrNull() {
        RaceProgressDTO raceProgress = null;
        if(raceDefinition != null && raceDefinition.getCourse() != null && trackedRace != null) {
            int totalLegsCount = raceDefinition.getCourse().getLegs().size();
            int currentLegNo = trackedRace.getLastLegStarted(MillisecondsTimePoint.now());
            if (currentLegNo > 0 && totalLegsCount > 0) {
                raceProgress = new RaceProgressDTO(currentLegNo, totalLegsCount);
            }
        }
        return raceProgress;
    }

    private String getCourseNameOrNull() {
        CourseBase lastCourse = state.getCourseDesign();
        if (lastCourse != null) {
            return lastCourse.getName();
        }
        return null;
    }
    
    public TimePoint getStartTime() {
        TimePoint startTime = null;
        if (trackedRace != null) {
            startTime = trackedRace.getStartOfRace();
        }
        if (startTime == null && state != null) {
            startTime = state.getStartTime();
        }
        return startTime;
    }

    private TimePoint getFinishTime() {
        TimePoint finishTime = null;
        if (trackedRace != null) {
            finishTime = trackedRace.getEndOfRace();
        } else if (state != null) {
            finishTime = state.getFinishedTime();
        } 
        return finishTime;
    }

    public void addLiveRace(LiveRacesDTO result) {
        TimePoint startTime = getStartTime();
        TimePoint finishTime = getFinishTime();
            // a race is of 'public interest' of a race is a combination of it's 'live' state
        // and special flags states indicating how the postponed/canceled races will be continued
        if(isLiveOrOfPublicInterest(startTime, finishTime)) {
            // the start time is always given for live races
            LiveRaceDTO liveRaceDTO = new LiveRaceDTO(getRegattaName(), raceColumn.getName());
            liveRaceDTO.setViewState(getRaceViewState(startTime, finishTime));
            liveRaceDTO.setRegattaDisplayName(getRegattaDisplayName());
            liveRaceDTO.setTrackedRaceName(trackedRace != null ? trackedRace.getRaceIdentifier().getRaceName() : null);
            liveRaceDTO.setTrackingState(getRaceTrackingState());
            liveRaceDTO.setFleet(getFleetMetadataOrNull());
            liveRaceDTO.setStart(startTime.asDate());
            liveRaceDTO.setBoatClass(getBoatClassName());
            liveRaceDTO.setCourseArea(getCourseAreaOrNull());
            liveRaceDTO.setCourse(getCourseNameOrNull());
            liveRaceDTO.setFlagState(getFlagStateOrNull());
            liveRaceDTO.setProgress(getProgressOrNull());
            liveRaceDTO.setWind(getWindOrNull());
            
            result.addRace(liveRaceDTO);        
        }
    }
    
    private boolean isLiveOrOfPublicInterest(TimePoint startTime, TimePoint finishTime) {
        boolean result = false;
        if(startTime != null) {
            if(trackedRace != null && trackedRace.hasGPSData() && trackedRace.hasWindData()) {
                result = trackedRace.isLive(now);
            } else {
                // no data from tracking but maybe a manual setting of the start and finish time
                TimePoint startOfLivePeriod = startTime.minus(TimingConstants.PRE_START_PHASE_DURATION_IN_MILLIS);
                TimePoint endOfLivePeriod = finishTime != null ? finishTime.plus(TimingConstants.IS_LIVE_GRACE_PERIOD_IN_MILLIS) : null; 
                if(now.after(startOfLivePeriod) && (endOfLivePeriod == null || now.before(endOfLivePeriod))) {
                    result = true;
                }
            }

        } else if (raceLog != null) {
            // in case there is not start time set it could be an postponed or abandoned race
            RaceLogFlagEvent abortingFlagEvent = checkForAbortFlagEvent();
            if(abortingFlagEvent != null) {
                TimePoint abortingTimeInPassBefore = abortingFlagEvent.getLogicalTimePoint();
                if (now.minus(abortingTimeInPassBefore.asMillis()).asMillis() < TIME_TO_SHOW_CANCELED_RACES_AS_LIVE) {
                    result = true;
                    // TODO: Problem: This causes the race added to the live races list without having a start time!!!
                    // This does not work right now -> consider using a start time of the last pass. 
                }
            }
        }

        return result;
    }

    public RaceTrackingState getRaceTrackingState() {
        RaceTrackingState trackingState = RaceTrackingState.NOT_TRACKED;
        if(trackedRace != null) {
            trackingState = RaceTrackingState.TRACKED_NO_VALID_DATA;
            if(trackedRace.hasWindData() && trackedRace.hasGPSData()) {
                trackingState = RaceTrackingState.TRACKED_VALID_DATA;
            }
        }
        return trackingState;
    }
    
    private RaceViewState getRaceViewState(TimePoint startTime, TimePoint finishTime) {
        RaceViewState raceState = RaceViewState.RUNNING;
        if (startTime != null && now.before(startTime)) {
            raceState = RaceViewState.SCHEDULED;
        } else if (finishTime != null && now.after(finishTime)) {
            raceState = RaceViewState.FINISHED;
        } else {
            // TODO: Resolve ABORTED and POSTPONED states
        }
        return raceState;
    }
    
    private Wind checkForWindFixesFromRaceLog() {
        WindFixesFinder windFixesFinder = new WindFixesFinder(raceLog);
        List<Wind> windList = windFixesFinder.analyze();
        if(windList.size() > 0) {
            return windList.get(windList.size()-1);
        }
        return null;
    }
    
    private RaceLogFlagEvent checkForAbortFlagEvent() {
        RaceLogFlagEvent result = null;
        if(raceLog != null) {
            AbortingFlagFinder abortingFlagFinder = new AbortingFlagFinder(raceLog);
            RaceLogFlagEvent abortingFlagEvent = abortingFlagFinder.analyze();
            if (abortingFlagEvent != null) {
                RaceLogRaceStatus lastStatus = state.getStatus();
                if (lastStatus.equals(RaceLogRaceStatus.UNSCHEDULED)) {
                    result = abortingFlagEvent;
                }
            }
        }
        return result;
    }

    public String getStageText() {
        // TODO fleet
        return getRegattaDisplayName() + " - " + raceColumn.getName();
    }

    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return trackedRace.getRaceIdentifier();
    }
}
