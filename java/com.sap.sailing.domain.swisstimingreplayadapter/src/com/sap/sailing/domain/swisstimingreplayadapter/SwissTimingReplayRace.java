package com.sap.sailing.domain.swisstimingreplayadapter;

import java.util.Date;

import com.sap.sse.common.Named;

public interface SwissTimingReplayRace extends Named {

    String getJsonUrl();

    String getFlightNumber();

    String getRaceId();

    String getRsc();

    String getBoatClass();

    Date getStartTime();

    String getLink();

}
