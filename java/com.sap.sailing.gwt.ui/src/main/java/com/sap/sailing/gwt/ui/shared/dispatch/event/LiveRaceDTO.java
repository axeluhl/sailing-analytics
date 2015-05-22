package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;

public class LiveRaceDTO extends RaceMetadataDTO {
    
    private FlagStateDTO flagState;
    
    private SimpleWindDTO wind;
    
    private RaceProgressDTO progress;

    @SuppressWarnings("unused")
    private LiveRaceDTO() {
    }
    
    public LiveRaceDTO(String regattaName, String raceName) {
        super(regattaName, raceName);
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
