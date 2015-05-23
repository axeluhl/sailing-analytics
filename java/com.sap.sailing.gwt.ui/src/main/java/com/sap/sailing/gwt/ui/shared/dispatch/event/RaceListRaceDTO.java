package com.sap.sailing.gwt.ui.shared.dispatch.event;

import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.WindStatisticsDTO;

public class RaceListRaceDTO extends RaceMetadataDTO {
    
    private FlagStateDTO flagState;
    
    private WindStatisticsDTO wind;
    
    @SuppressWarnings("unused")
    private RaceListRaceDTO() {
    }
    
    public RaceListRaceDTO(String regattaName, String raceName) {
        super(regattaName, raceName);
    }

    public WindStatisticsDTO getWind() {
        return wind;
    }

    public void setWind(WindStatisticsDTO wind) {
        this.wind = wind;
    }

    public FlagStateDTO getFlagState() {
        return flagState;
    }

    public void setFlagState(FlagStateDTO flagState) {
        this.flagState = flagState;
    }
}
