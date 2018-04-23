package com.sap.sailing.gwt.ui.shared;

import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.BoatClassDTO;

public class StrippedLeaderboardDTO extends AbstractLeaderboardDTO {
    public StrippedLeaderboardDTO(BoatClassDTO boatClass) {
        super(boatClass);
    }

    private static final long serialVersionUID = 3285029720177137625L;

    public int competitorsCount;

    public String boatClassName;
    
    public void removeRace(String raceColumnName) {
        getRaceList().remove(getRaceColumnByName(raceColumnName));
    }
}
