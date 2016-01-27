package com.sap.sailing.dashboards.gwt.shared.dto;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.dispatch.client.Result;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class RaceIdDTO implements Result {

    private RegattaAndRaceIdentifier raceId;

    public RaceIdDTO() {}

    public RegattaAndRaceIdentifier getRaceId() {
        return raceId;
    }

    public void setRaceId(RegattaAndRaceIdentifier raceId) {
        this.raceId = raceId;
    }
}
