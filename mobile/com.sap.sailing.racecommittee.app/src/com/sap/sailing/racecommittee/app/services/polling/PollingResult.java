package com.sap.sailing.racecommittee.app.services.polling;

import java.io.InputStream;
import java.io.Serializable;

import com.sap.sailing.domain.common.impl.Util.Pair;

public class PollingResult {
    public final boolean isSuccess;
    public final Pair<Serializable, InputStream> resultStreamForRaceId;
    
    public PollingResult(boolean isSuccess, Pair<Serializable, InputStream> resultStreamForRaceId) {
        this.isSuccess = isSuccess;
        this.resultStreamForRaceId = resultStreamForRaceId;
    }
}
