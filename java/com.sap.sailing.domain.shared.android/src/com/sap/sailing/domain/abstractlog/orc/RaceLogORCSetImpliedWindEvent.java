package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sse.common.Speed;

public interface RaceLogORCSetImpliedWindEvent extends RaceLogEvent, Revokable, InvalidatesLeaderboardCache {
    Speed getImpliedWindSpeed();
}
