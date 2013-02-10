package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public interface FlexibleRaceColumn extends RaceColumn {
    void setName(String newName);
    void setIsMedalRace(boolean isMedalRace);
    
    void recordRaceLogEvent(Fleet fleet, RaceLogEvent event);
}
