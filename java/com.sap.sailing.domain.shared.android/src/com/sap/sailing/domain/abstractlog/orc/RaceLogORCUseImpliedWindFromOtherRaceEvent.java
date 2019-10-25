package com.sap.sailing.domain.abstractlog.orc;

import com.sap.sailing.domain.abstractlog.Revokable;
import com.sap.sailing.domain.abstractlog.race.InvalidatesLeaderboardCache;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;

public interface RaceLogORCUseImpliedWindFromOtherRaceEvent extends RaceLogEvent, Revokable, InvalidatesLeaderboardCache {
    SimpleRaceLogIdentifier getOtherRace();
}
