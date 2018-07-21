package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.base.RaceColumnListener;

public interface HasRaceColumns {
    Iterable<? extends RaceColumn> getRaceColumns();
    
    void addRaceColumnListener(RaceColumnListener listener);
    
    void removeRaceColumnListener(RaceColumnListener listener);
}
