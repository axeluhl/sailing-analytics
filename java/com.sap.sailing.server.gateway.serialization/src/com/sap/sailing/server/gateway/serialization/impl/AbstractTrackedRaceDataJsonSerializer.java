package com.sap.sailing.server.gateway.serialization.impl;

import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public abstract class AbstractTrackedRaceDataJsonSerializer implements JsonSerializer<TrackedRace>{

    public static final String TIMEASISO = "timeasiso";
    public static final String TIMEASMILLIS = "timeasmillis";
    public static final String COMPETITOR = "competitor";
    public static final String BYCOMPETITOR = "bycompetitor";

}
