package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.UUID;

import com.sap.sailing.gwt.ui.shared.dispatch.Action;
import com.sap.sailing.gwt.ui.shared.dispatch.ResultWithTTL;

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
}
