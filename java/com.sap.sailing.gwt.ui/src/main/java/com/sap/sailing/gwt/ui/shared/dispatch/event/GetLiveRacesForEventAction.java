package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.DispatchContext;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RacesActionUtil.RaceCallback;
import com.sap.sse.common.TimePoint;

public class GetLiveRacesForEventAction implements Action<ResultWithTTL<LiveRacesDTO>> {
    private UUID eventId;
    
    public GetLiveRacesForEventAction() {
    }

    public GetLiveRacesForEventAction(UUID eventId) {
        this.eventId = eventId;
    }

    @Override
    @GwtIncompatible
    public ResultWithTTL<LiveRacesDTO> execute(DispatchContext context) {
        final LiveRacesDTO result = new LiveRacesDTO();
        
        RacesActionUtil.forRacesOfEvent(context, eventId, new RaceCallback() {
            @Override
            public void doForRace(RaceContext rc) {
                if(!rc.isRaceDefinitionAvailable() || !rc.isRaceLogAvailable() || !rc.isLive()) {
                    return;
                }
                
                TimePoint startTime = rc.getStartTime();
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
