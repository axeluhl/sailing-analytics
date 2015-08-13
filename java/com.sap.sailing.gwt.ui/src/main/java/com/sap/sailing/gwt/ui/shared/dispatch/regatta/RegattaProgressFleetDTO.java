package com.sap.sailing.gwt.ui.shared.dispatch.regatta;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RegattaProgressFleetDTO implements DTO {
    private int finishedRaceCount;
    private int liveRaceCount;
    
    @SuppressWarnings("unused")
    private RegattaProgressFleetDTO() {
    }
    
    public RegattaProgressFleetDTO(int finishedRaceCount, int liveRaceCount) {
        this.finishedRaceCount = finishedRaceCount;
        this.liveRaceCount = liveRaceCount;
    }
    
    public int getFinishedRaceCount() {
        return finishedRaceCount;
    }
    
    public int getLiveRaceCount() {
        return liveRaceCount;
    }
    
    public int getFinishedAndLiveRaceCount() {
        return finishedRaceCount + liveRaceCount;
    }
}
