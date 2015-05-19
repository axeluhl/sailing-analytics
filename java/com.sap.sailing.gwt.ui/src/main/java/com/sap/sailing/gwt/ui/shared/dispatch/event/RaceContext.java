package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
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
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.LiveRaceState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
public class RaceContext {
    private final MillisecondsTimePoint now = MillisecondsTimePoint.now();
    private final Leaderboard lb;
    private final RaceColumn raceColumn;
    private final Fleet fleet;
    private final RaceDefinition raceDefinition;
    private final TrackedRace trackedRace;
    private final RaceLog raceLog;
    private final ReadonlyRaceState state;
    private final Event event;

    public RaceContext(Event event, Leaderboard lb, RaceColumn raceColumn, Fleet fleet) {
        this.event = event;
        this.lb = lb;
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
        if (lb instanceof RegattaLeaderboard) {
            Regatta regatta = ((RegattaLeaderboard) lb).getRegatta();
            return regatta.getName();
        }
        return lb.getName();
    }

    private String getRegattaDisplayName() {
        String displayName = lb.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        return lb.getName();
    }

    private RegattaAndRaceIdentifier getIdentifier() {
        return new RegattaNameAndRaceName(getRegattaName(), raceColumn.getName());
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
                    return new SimpleWindDTO(wind.getBearing().reverse().getDegrees(), wind.getKnots());
                }
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
        if (lb instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) lb).getRegatta();
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
        for (TrackedRace trackedRace : lb.getTrackedRaces()) {
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
        if(lb instanceof FlexibleLeaderboard) {
            courseArea = ((FlexibleLeaderboard)lb).getDefaultCourseArea();
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
            raceProgress = new RaceProgressDTO(currentLegNo, totalLegsCount);
        }
        return raceProgress;
    }

    private String getCourseNameOrNull() {
        String courseName = null;
        if(raceDefinition != null && raceDefinition.getCourse() != null) {
            courseName = raceDefinition.getCourse().getName();
        }
        return courseName;
    }
    
    private boolean isRaceLogAvailable() {
        return raceLog != null;
    }
    
    private boolean isStartTimeAvailable() {
        return getStartTime() != null;
    }

    private TimePoint getStartTime() {
        TimePoint startTime = null;
        if (trackedRace != null) {
            startTime = trackedRace.getStartOfRace();
        } else if (state != null) {
            startTime = state.getStartTime();
        }
        return startTime;
    }

    /**
     * The 'public interest' of a race is a combination of it's 'live' state
     * and special flags states indicating how the postponed/canceled races will be continued
     * @return
     */
    public boolean isOfPublicInterest() {
        // TODO better condition after Frank implemented race state stuff
        return isStartTimeAvailable() && isRaceLogAvailable();
    }

    public LiveRaceDTO getLiveRaceDTO() {
        // the start time is always given for live races
        TimePoint startTime = getStartTime();
        LiveRaceState raceState = LiveRaceState.LIVE;
        if (now.before(startTime)) {
            raceState = LiveRaceState.UPCOMING;
        } else if (state != null && state.getStatus() == RaceLogRaceStatus.FINISHED) {
            raceState = LiveRaceState.FINISHED;
        } else if (trackedRace != null && trackedRace.getEndOfRace() != null && now.after(trackedRace.getEndOfRace())) {
            raceState = LiveRaceState.FINISHED;
        }
        LiveRaceDTO liveRaceDTO = new LiveRaceDTO(getIdentifier());
        liveRaceDTO.setState(raceState);
        liveRaceDTO.setRegattaName(getRegattaDisplayName());
        liveRaceDTO.setFleet(getFleetMetadataOrNull());
        liveRaceDTO.setRaceName(raceColumn.getName());
        liveRaceDTO.setStart(startTime.asDate());
        liveRaceDTO.setBoatClass(getBoatClassName());
        liveRaceDTO.setCourseArea(getCourseAreaOrNull());
        liveRaceDTO.setCourse(getCourseNameOrNull());
        liveRaceDTO.setFlagState(getFlagStateOrNull());
        liveRaceDTO.setProgress(getProgressOrNull());
        liveRaceDTO.setWind(getWindOrNull());
        return liveRaceDTO;
    }
}