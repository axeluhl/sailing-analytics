package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatClassDTO;

public class StrippedLeaderboardDTO extends AbstractLeaderboardDTO {
    private static final long serialVersionUID = 3285029720177137625L;

    public int competitorsCount;

    public String boatClassName;
    
    @Deprecated
    StrippedLeaderboardDTO() {
        super(); // for GWT serialization only
    }
    
    public StrippedLeaderboardDTO(BoatClassDTO boatClass) {
        super(boatClass);
    }
    
    public void removeRace(String raceColumnName) {
        getRaceList().remove(getRaceColumnByName(raceColumnName));
    }
}
