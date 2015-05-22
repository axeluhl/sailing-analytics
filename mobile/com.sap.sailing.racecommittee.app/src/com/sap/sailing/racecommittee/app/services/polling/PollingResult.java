package com.sap.sailing.racecommittee.app.services.polling;

import com.sap.sse.common.Util;

import java.io.InputStream;
import java.io.Serializable;

public class PollingResult {
    public final boolean isSuccess;
    public final Util.Pair<Serializable, InputStream> resultStreamForRaceId;
    
    public PollingResult(boolean isSuccess, Util.Pair<Serializable, InputStream> resultStreamForRaceId) {
        this.isSuccess = isSuccess;
        this.resultStreamForRaceId = resultStreamForRaceId;
    }
}
