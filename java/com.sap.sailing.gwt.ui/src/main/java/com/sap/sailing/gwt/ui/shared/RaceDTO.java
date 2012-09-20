package com.sap.sailing.gwt.ui.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.tracking.RaceTracker;
import com.sap.sailing.domain.tracking.TrackedRace;

public class RaceDTO extends NamedDTO implements IsSerializable {
    /**
     * Tells if this race is currently being tracked, meaning that a {@link RaceTracker} is
     * listening for incoming GPS fixes, mark passings etc., to update a {@link TrackedRace} object
     * accordingly.
     */
    public boolean isTracked;

    public Date startOfRace;
    public Date endOfRace;

    public PlacemarkOrderDTO places;

    public TrackedRaceDTO trackedRace;

    private String regattaName;
    public String boatClass;
    
    public RaceDTO() {}

    public RaceDTO(RegattaAndRaceIdentifier raceIdentifier) {
        this(raceIdentifier, null);
    }

    public RaceDTO(RegattaAndRaceIdentifier raceIdentifier, TrackedRaceDTO trackedRace) {
        super(raceIdentifier.getRaceName());
        this.regattaName = raceIdentifier.getRegattaName();
        this.trackedRace = trackedRace;
        this.isTracked = trackedRace != null;
    }

    public RegattaAndRaceIdentifier getRaceIdentifier() {
        return new RegattaNameAndRaceName(regattaName, name);
    }

    public String getRegattaName() {
        return regattaName;
    }
}
