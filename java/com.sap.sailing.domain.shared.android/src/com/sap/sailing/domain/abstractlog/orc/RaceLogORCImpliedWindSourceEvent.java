package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.race.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.common.orc.ImpliedWindSource;

public interface RaceLogORCImpliedWindSourceEvent extends RaceLogEvent, InvalidatesLeaderboardCache {
    ImpliedWindSource getImpliedWindSource();
}
