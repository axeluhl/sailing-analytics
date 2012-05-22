package com.sap.sailing.domain.leaderboard.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.AbstractRaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;

public class RaceColumnImpl extends AbstractRaceColumn implements FlexibleRaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    
    /**
     * All fleets for which this column can contain a race. This is the maximum set of keys possible for
     * {@link #trackedRaces} and {@link #raceIdentifiers}.
     */
    private final Iterable<Fleet> fleets;
    private boolean medalRace;
    
    public RaceColumnImpl(String name, boolean medalRace, Iterable<Fleet> fleets) {
        super(name);
        this.medalRace = medalRace;
        this.fleets = fleets;
    }
    
    @Override
    public boolean isMedalRace() {
        return medalRace;
    }

    @Override
    public void setIsMedalRace(boolean isMedalRace) {
        this.medalRace = isMedalRace;
    }

    @Override
    public Iterable<Fleet> getFleets() {
        return fleets;
    }

}
