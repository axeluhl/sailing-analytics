package com.sap.sailing.domain.swisstimingreplayadapter;

import java.util.Date;

import com.sap.sailing.domain.common.Named;

public interface SwissTimingReplayRace extends Named {

    String getJsonUrl();

    String getFlightNumber();

    String getRaceId();

    String getRsc();

    String getBoatClass();

    Date getStartTime();

    String getLink();

}
