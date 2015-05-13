package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.List;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Leg;
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
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

@GwtIncompatible
public class RaceContext {
    private final MillisecondsTimePoint now = MillisecondsTimePoint.now();
    public final LeaderboardGroup lg;
    public final Leaderboard lb;
    public final RaceColumn raceColumn;
    public final Fleet fleet;
    public final RaceDefinition raceDefinition;
    public final TrackedRace trackedRace;
    private final RaceLog raceLog;
    private final ReadonlyRaceState state;

    public RaceContext(LeaderboardGroup lg, Leaderboard lb, RaceColumn raceColumn, Fleet fleet) {
        this.lg = lg;
        this.lb = lb;
        this.raceColumn = raceColumn;
        this.raceDefinition = raceColumn.getRaceDefinition(fleet);
        this.fleet = fleet;
        trackedRace = raceColumn.getTrackedRace(fleet);
        raceLog = raceColumn.getRaceLog(fleet);
        state = ReadonlyRaceStateImpl.create(raceLog);
    }

    public boolean isShowFleetData() {
        return !LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleet.getName());
    }

    public String getRegattaName() {
        if (lb instanceof RegattaLeaderboard) {
            Regatta regatta = ((RegattaLeaderboard) lb).getRegatta();
            return regatta.getName();
        }
        return lb.getName();
    }

    public String getRegattaDisplayName() {
        String displayName = lb.getDisplayName();
        if (displayName != null && !displayName.isEmpty()) {
            return displayName;
        }
        return lb.getName();
    }

    public RegattaAndRaceIdentifier getIdentifier() {
        return new RegattaNameAndRaceName(getRegattaName(), raceDefinition.getName());
    }

    public FleetMetadataDTO getFleetMetadataOrNull() {
        if (!isShowFleetData()) {
            return null;
        }
        return new FleetMetadataDTO(fleet.getName(), fleet.getColor() == null ? null : fleet.getColor().getAsHtml());
    }

    public SimpleWindDTO getWindOrNull() {
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
        return null;
    }

    public FlagStateDTO getFlagStateOrNull() {
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
    
    public Regatta getRegatta() {
        final Regatta regatta;
        if (lb instanceof RegattaLeaderboard) {
            regatta = ((RegattaLeaderboard) lb).getRegatta();
        } else {
            regatta = null;
        }
        return regatta;
    }
    
    public String getBoatClassName() {
        BoatClass boatClass = getBoatClass();
        return boatClass == null ? null : boatClass.getName();
    }

    public BoatClass getBoatClass() {
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

    public String getCourseArea() {
        Regatta regatta = getRegatta();
        if(regatta != null && regatta.getDefaultCourseArea() != null) {
            return regatta.getDefaultCourseArea().getName();
        }
        return null;
    }

    public RaceProgressDTO getProgressOrNull() {
        List<Leg> legs = state.getCourseDesign() != null ? state.getCourseDesign().getLegs() : (raceDefinition.getCourse() != null ? raceDefinition.getCourse().getLegs() : null);
        if(legs != null && trackedRace != null) {
            return new RaceProgressDTO(trackedRace.getLastLegStarted(MillisecondsTimePoint.now()), legs.size());
        }
        return null;
    }

    public String getCourseName() {
        Course course = raceDefinition.getCourse();
        if(course != null) {
            return course.getName();
        }
        return null;
    }
    
    public boolean isRaceDefinitionAvailable() {
        return raceDefinition != null;
    }
    
    public boolean isRaceLogAvailable() {
        return raceLog != null;
    }
    
    public boolean isStartTimeAvailable() {
        return getStartTime() != null;
    }

    public boolean isLive() {
        // TODO
//        return RaceLogRaceStatus.isActive(state.getStatus());
        return true;
    }

    public TimePoint getStartTime() {
        if(state == null) {
            return null;
        }
        return state.getStartTime();
    }

    public LiveRaceDTO getLiveRaceDTO() {
        TimePoint startTime = getStartTime();
        RaceState raceState = RaceState.LIVE;
        if(startTime == null || now.before(startTime)) {
            raceState = RaceState.UPCOMING;
        } else if(trackedRace.getEndOfRace() != null && now.after(trackedRace.getEndOfRace())) {
            raceState = RaceState.FINISHED;
        }
        LiveRaceDTO liveRaceDTO = new LiveRaceDTO(getIdentifier());
        liveRaceDTO.setState(raceState);
        liveRaceDTO.setRegattaName(getRegattaDisplayName());
        liveRaceDTO.setFleet(getFleetMetadataOrNull());
        liveRaceDTO.setRaceName(raceColumn.getName());
        liveRaceDTO.setStart(startTime.asDate());
        liveRaceDTO.setBoatClass(getBoatClassName());
        liveRaceDTO.setCourseArea(getCourseArea());
        liveRaceDTO.setCourse(getCourseName());
        liveRaceDTO.setFlagState(getFlagStateOrNull());
        liveRaceDTO.setProgress(getProgressOrNull());
        liveRaceDTO.setWind(getWindOrNull());
        return liveRaceDTO;
    }
}