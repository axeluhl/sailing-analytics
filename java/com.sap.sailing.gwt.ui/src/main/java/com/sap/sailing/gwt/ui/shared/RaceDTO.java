package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;

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
    public Date endOfTracking;
    public Date timePointOfNewestEvent;
    public Date endOfRace;
    public long delayToLiveInMs;
    
    private RegattaDTO regatta;
    
    public RaceDTO() {}

    public RaceDTO(String name, Iterable<CompetitorDTO> competitors, boolean currentlyTracked) {
        super(name);
        this.competitors = competitors;
        this.currentlyTracked = currentlyTracked;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
    }
    
    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return new RegattaNameAndRaceName(regatta.name, name);
    }
    
    public RegattaDTO getRegatta() {
        return regatta;
    }
}
