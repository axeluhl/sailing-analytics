package com.sap.sailing.gwt.ui.client;

import java.util.Map;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public interface RaceTimesInfoProviderListener {

    public void raceTimesInfosReceived(Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfo);
    
}
