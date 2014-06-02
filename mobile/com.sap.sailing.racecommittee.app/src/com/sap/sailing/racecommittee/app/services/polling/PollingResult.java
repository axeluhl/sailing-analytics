package com.sap.sailing.racecommittee.app.services.polling;

import java.io.InputStream;
import java.io.Serializable;

import com.sap.sse.common.UtilNew;

public class PollingResult {
    public final boolean isSuccess;
    public final UtilNew.Pair<Serializable, InputStream> resultStreamForRaceId;
    
    public PollingResult(boolean isSuccess, UtilNew.Pair<Serializable, InputStream> resultStreamForRaceId) {
        this.isSuccess = isSuccess;
        this.resultStreamForRaceId = resultStreamForRaceId;
    }
}
