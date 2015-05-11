package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;

public class LiveRaceDTO extends RaceMetadataDTO {
    
    private FlagStateDTO flagState;
    
    private SimpleWindDTO wind;
    
    private String boatClass;
    
    private RaceProgressDTO progress;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(RegattaAndRaceIdentifier id) {
        super(id);
    }

    public SimpleWindDTO getWind() {
        return wind;
    }

    public void setWind(SimpleWindDTO wind) {
        this.wind = wind;
    }

    public FlagStateDTO getFlagState() {
        return flagState;
    }

    public void setFlagState(FlagStateDTO flagState) {
        this.flagState = flagState;
    }

    public String getBoatClass() {
        return boatClass;
    }

    public void setBoatClass(String boatClass) {
        this.boatClass = boatClass;
    }

    public RaceProgressDTO getProgress() {
        return progress;
    }

    public void setProgress(RaceProgressDTO progress) {
        this.progress = progress;
    }
}
