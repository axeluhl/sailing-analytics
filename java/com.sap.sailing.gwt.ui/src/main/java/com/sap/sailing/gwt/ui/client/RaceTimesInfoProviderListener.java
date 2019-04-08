package com.sap.sailing.gwt.ui.client;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public interface RaceTimesInfoProviderListener {

    public void raceTimesInfosReceived(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo, long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived);
    
}
