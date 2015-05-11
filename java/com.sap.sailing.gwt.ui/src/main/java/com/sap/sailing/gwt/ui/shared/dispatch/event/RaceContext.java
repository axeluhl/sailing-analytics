package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.Iterator;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.RaceColumn;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;

public class RaceContext {
    public final LeaderboardGroup lg;
    public final Leaderboard lb;
    public final RaceColumn raceColumn;
    public final String regattaName;
    public final Fleet fleet;

    public RaceContext(LeaderboardGroup lg, Leaderboard lb, RaceColumn raceColumn, String regattaName, Fleet fleet) {
        this.lg = lg;
        this.lb = lb;
        this.raceColumn = raceColumn;
        this.regattaName = regattaName;
        this.fleet = fleet;
    }
    
    public boolean isSingleFleet() {
        Iterable<? extends Fleet> fleets = raceColumn.getFleets();
        
        if(fleets instanceof Collection) {
            return ((Collection<?>) fleets).size() <= 1;
        }
        
        if(fleets == null) {
            return false;
        }
        Iterator<? extends Fleet> fleetsIterator = fleets.iterator();
        if(!fleetsIterator.hasNext()) {
            return false;
        }
        fleetsIterator.next();
        return !fleetsIterator.hasNext();
    }
}