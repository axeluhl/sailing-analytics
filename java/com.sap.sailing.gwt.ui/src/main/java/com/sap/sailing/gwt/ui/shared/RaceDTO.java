package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.EventNameAndRaceName;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RacePlaceOrder;

public class RaceDTO extends NamedDTO implements IsSerializable {
    public Iterable<CompetitorDTO> competitors;
    
    /**
     * Tells if this race is currently being tracked live, meaning that a {@link TracTracRaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean currentlyTracked;

    public Date startOfRace;
    public Date startOfTracking;
    public Date timePointOfLastEvent;
    public Date timePointOfNewestEvent;
    
    public RacePlaceOrder racePlaces = null;
    
    private RegattaDTO regatta;
    
    public RaceDTO() {}

    public RaceDTO(String name, Iterable<CompetitorDTO> competitors, boolean currentlyTracked) {
        super(name);
        this.competitors = competitors;
        this.currentlyTracked = currentlyTracked;
    }

    public RegattaDTO getRegatta() {
        return regatta;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
    }
    
    public RaceIdentifier getRaceIdentifier() {
        return new EventNameAndRaceName(regatta.getEvent().name, name);
    }
    
    public EventDTO getEvent() {
        return regatta.getEvent();
    }
}
