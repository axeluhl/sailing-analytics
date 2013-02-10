package com.sap.sailing.domain.base;

import com.sap.sailing.domain.racelog.RaceColumnIdentifier;

public interface RaceColumnInSeries extends RaceColumn {
    Series getSeries();
    
    Regatta getRegatta();
    
    RaceColumnIdentifier getRaceColumnIdentifier(String leaderboardName);
}
