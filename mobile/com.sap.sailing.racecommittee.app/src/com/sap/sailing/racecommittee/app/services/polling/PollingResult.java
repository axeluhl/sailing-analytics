package com.sap.sailing.racecommittee.app.services.polling;

import java.io.InputStream;

import com.sap.sse.common.Util;

public class PollingResult {
    public final boolean isSuccess;
    public final Util.Pair<String, InputStream> resultStreamForRaceId;
    
    public PollingResult(boolean isSuccess, Util.Pair<String, InputStream> resultStreamForRaceId) {
        this.isSuccess = isSuccess;
        this.resultStreamForRaceId = resultStreamForRaceId;
    }
}
