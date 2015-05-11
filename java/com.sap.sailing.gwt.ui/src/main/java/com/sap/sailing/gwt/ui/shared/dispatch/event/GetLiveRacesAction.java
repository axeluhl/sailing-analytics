package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.List;
import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.WindSourceImpl;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindTrack;
import com.sap.sailing.domain.tracking.WindWithConfidence;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sse.common.TimePoint;
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
        final MillisecondsTimePoint now = MillisecondsTimePoint.now();
        final LiveRacesDTO result = new LiveRacesDTO();
        
        RacesActionUtil.forRacesOfEvent(context, getEventId(), new RaceCallback() {
            @Override
            public void doForRace(RaceContext rc) {
                RaceDefinition raceDefinition = rc.raceColumn.getRaceDefinition(rc.fleet);
                if(raceDefinition == null) {
                    return;
                }
                final RaceLog raceLog = rc.raceColumn.getRaceLog(rc.fleet);
                if(raceLog == null) {
                    // No racelog -> we can't decide if the race is live
                    return;
                }
                final ReadonlyRaceState state = ReadonlyRaceStateImpl.create(raceLog);
                if(!RaceLogRaceStatus.isActive(state.getStatus())) {
                    // TODO Unscheduled -> what to do with an abandoned race
                    // race isn't live
                    return;
                }
                TrackedRace trackedRace = rc.raceColumn.getTrackedRace(rc.fleet);
                
                TimePoint startTime = state.getStartTime();
                if(startTime == null) {
                    return;
                }
                
                LiveRaceDTO liveRaceDTO = new LiveRaceDTO(new RegattaNameAndRaceName(rc.regattaName, raceDefinition.getName()));
                liveRaceDTO.setRegattaName(rc.regattaName);
                
                if(!rc.isSingleFleet()) {
                    liveRaceDTO.setFleetName(rc.fleet.getName());
                    liveRaceDTO.setFleetColor(rc.fleet.getColor() == null ? null : rc.fleet.getColor().getAsHtml());
                }
                
                liveRaceDTO.setRaceName(rc.raceColumn.getName());
                
                liveRaceDTO.setStart(startTime.asDate());
                
                Course course = raceDefinition.getCourse();
                if(course != null) {
                    liveRaceDTO.setCourseArea(course.getName());
                }
                
                FlagPoleState activeFlagState = state.getRacingProcedure().getActiveFlags(startTime, now);
                List<FlagPole> activeFlags = activeFlagState.getCurrentState();
                FlagPoleState previousFlagState = activeFlagState.getPreviousState(state.getRacingProcedure(), startTime);
                List<FlagPole> previousFlags = previousFlagState.getCurrentState();
                FlagPole mostInterestingFlagPole = FlagPoleState.getMostInterestingFlagPole(previousFlags, activeFlags);

                // TODO: adapt the LastFlagFinder#getMostRecent method!
                if (mostInterestingFlagPole != null) {
                    liveRaceDTO.setLastLowerFlag(mostInterestingFlagPole.getLowerFlag());
                    liveRaceDTO.setLastUpperFlag(mostInterestingFlagPole.getUpperFlag());
                    liveRaceDTO.setLastFlagsAreDisplayed(mostInterestingFlagPole.isDisplayed());
                    liveRaceDTO.setLastFlagsDisplayedStateChanged(previousFlagState.hasPoleChanged(mostInterestingFlagPole));
                }
                
                List<Leg> legs = state.getCourseDesign() != null ? state.getCourseDesign().getLegs() : (raceDefinition.getCourse() != null ? raceDefinition.getCourse().getLegs() : null);
                if(legs != null && trackedRace != null) {
                    liveRaceDTO.setTotalLegs(legs.size());
                    liveRaceDTO.setCurrentLeg(trackedRace.getLastLegStarted(MillisecondsTimePoint.now()));
                }
                
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
                        liveRaceDTO.setTrueWindSpeedInKnots(wind.getKnots());
                        liveRaceDTO.setTrueWindFromDeg(wind.getBearing().reverse().getDegrees());
                    }
                }
                
                result.addRace(liveRaceDTO);
            }
        });
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }
}
