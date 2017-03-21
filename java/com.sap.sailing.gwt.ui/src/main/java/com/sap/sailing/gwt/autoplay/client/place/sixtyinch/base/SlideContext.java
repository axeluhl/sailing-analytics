package com.sap.sailing.gwt.autoplay.client.place.sixtyinch.base;

import java.util.Date;
import java.util.Map;

import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.autoplay.client.place.sixtyinch.start.SixtyInchSetting;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProvider;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.RaceboardDataDTO;

public interface SlideContext {
    SixtyInchSetting getSettings();

    void updateEvent(EventDTO event);

    EventDTO getEvent();

    void updateRaceTimeInfos(Map<RegattaAndRaceIdentifier, RaceTimesInfoDTO> raceTimesInfo,
            long clientTimeWhenRequestWasSent, Date serverTimeDuringRequest, long clientTimeWhenResponseWasReceived,
            RegattaAndRaceIdentifier lifeRace, RaceboardDataDTO result);

    long getClientTimeWhenRequestWasSent();

    Date getServerTimeDuringRequest();

    long getClientTimeWhenResponseWasReceived();

    RegattaAndRaceIdentifier getCurrentLiveRace();

    RaceTimesInfoProvider getRaceTimesInfoProvider();

    void setRaceTimesInfoProvider(RaceTimesInfoProvider raceTimesInfoProvider);

    RaceboardDataDTO getRaceboardResult();
}
