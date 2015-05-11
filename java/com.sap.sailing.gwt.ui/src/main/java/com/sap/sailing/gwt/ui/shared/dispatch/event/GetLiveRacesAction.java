package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.ReadonlyRaceStateImpl;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sse.common.TimePoint;

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
//                if(!RaceLogRaceStatus.isActive(state.getStatus())) {
//                    // TODO Unscheduled -> what to do with an abandoned race
//                    // race isn't live
//                    return;
//                }
                
                TimePoint startTime = state.getStartTime();
                if(startTime == null) {
                    return;
                }
                
                LiveRaceDTO liveRaceDTO = new LiveRaceDTO(rc.getIdentifier());
                liveRaceDTO.setRegattaName(rc.getRegattaDisplayName());
                liveRaceDTO.setFleet(rc.getFleetMetadataOrNull());
                liveRaceDTO.setRaceName(rc.raceColumn.getName());
                liveRaceDTO.setStart(startTime.asDate());
                liveRaceDTO.setBoatClass(rc.getBoatClassName());
                liveRaceDTO.setCourseArea(rc.getCourseArea());
                liveRaceDTO.setCourse(rc.getCourseName());
                liveRaceDTO.setFlagState(rc.getFlagStateOrNull());
                liveRaceDTO.setProgress(rc.getProgressOrNull());
                liveRaceDTO.setWind(rc.getWindOrNull());
                
                result.addRace(liveRaceDTO);
            }
        });
        return new ResultWithTTL<LiveRacesDTO>(5000, result);
    }
}
