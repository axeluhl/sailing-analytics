package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;

public class LiveRaceDTO extends RaceMetadataDTO {
    
    private String regattaName;
    
    private FlagStateDTO flagState;
    
    private SimpleWindDTO wind;
    
    private RaceProgressDTO progress;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(RegattaAndRaceIdentifier id) {
        super(id);
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
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

    public RaceProgressDTO getProgress() {
        return progress;
    }

    public void setProgress(RaceProgressDTO progress) {
        this.progress = progress;
    }
}
