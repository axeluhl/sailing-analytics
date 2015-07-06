package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Date;

import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sse.common.Util;

public class LiveRaceDTO extends RaceMetadataDTO implements Comparable<LiveRaceDTO> {
    
    private FlagStateDTO flagState;
    
    private RaceProgressDTO progress;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(String regattaName, String raceName) {
        super(regattaName, raceName);
    }

    public FlagStateDTO getFlagState() {
        return flagState;
    }

    public void setFlagState(FlagStateDTO flagState) {
        this.flagState = flagState;
    }

    public RaceProgressDTO getProgress() {
        return progress;
    }

    public void setProgress(RaceProgressDTO progress) {
        this.progress = progress;
    }

    @Override
    public int compareTo(LiveRaceDTO o) {
        Date thisStart = getStart();
        Date otherStart = o.getStart();
        if(Util.equalsWithNull(thisStart, otherStart)) {
            // cases where both start times are == null or equal
            return compareBySecondaryCriteria(o);
        }
        if(thisStart == null) {
            return 1;
        }
        if(otherStart == null) {
            return -1;
        }
        return -thisStart.compareTo(otherStart);
    }

    private int compareBySecondaryCriteria(LiveRaceDTO o) {
        String thisRegattaName = getRegattaName();
        String otherRegattaName = o.getRegattaName();
        if(thisRegattaName != otherRegattaName) {
            if(thisRegattaName == null) {
                return 1;
            }
            if(otherRegattaName == null) {
                return -1;
            }
            int compareByRegatta = thisRegattaName.compareTo(otherRegattaName);
            if(compareByRegatta != 0) {
                return compareByRegatta;
            }
        }
        int compareByRace = getRaceName().compareTo(o.getRaceName());
        if(compareByRace != 0) {
            return compareByRace;
        }
        FleetMetadataDTO thisFleet = getFleet();
        FleetMetadataDTO otherFleet = o.getFleet();
        if(thisFleet != otherFleet) {
            if(thisFleet == null) {
                return 1;
            }
            if(otherFleet == null) {
                return -1;
            }
            int compareByFleet = thisFleet.compareTo(otherFleet);
            if(compareByFleet != 0) {
                return compareByFleet;
            }
        }
        return getViewState().compareTo(o.getViewState());
    }
}
