package com.sap.sailing.domain.base;

import com.sap.sailing.domain.racelog.RaceColumnIdentifier;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public interface RaceColumnInSeries extends RaceColumn {
    Series getSeries();
    
    Regatta getRegatta();
    
    RaceColumnIdentifier getRaceColumnIdentifier(String leaderboardName);
    
    void recordRaceLogEvent(String leaderboardName, Fleet fleet, RaceLogEvent event);
}
