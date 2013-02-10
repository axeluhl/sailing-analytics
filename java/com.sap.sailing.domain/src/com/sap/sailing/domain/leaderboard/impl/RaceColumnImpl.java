package com.sap.sailing.domain.leaderboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.AbstractRaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogStore;

public class RaceColumnImpl extends AbstractRaceColumn implements FlexibleRaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    private String name;

    /**
     * All fleets for which this column can contain a race. This is the maximum set of keys possible for
     * {@link #trackedRaces} and {@link #raceIdentifiers}.
     */
    private final Iterable<Fleet> fleets;
    private boolean medalRace;
    
    public RaceColumnImpl(String name, boolean medalRace, RaceLogStore raceLogStore, Iterable<Fleet> fleets) {
        super(raceLogStore);
        this.name = name;
        this.medalRace = medalRace;
        List<Fleet> myFleets = new ArrayList<Fleet>();
        for (Fleet fleet : fleets) {
            myFleets.add(fleet);
        }
        Collections.sort(myFleets);
        this.fleets = myFleets;
    }
    
    @Override
    public void setName(String newName) {
        this.name = newName;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public boolean isMedalRace() {
        return medalRace;
    }

    @Override
    public void setIsMedalRace(boolean isMedalRace) {
        this.medalRace = isMedalRace;
        getRaceColumnListeners().notifyListenersAboutIsMedalRaceChanged(this, isMedalRace());
    }

    @Override
    public Iterable<Fleet> getFleets() {
        return fleets;
    }
    
    @Override
	public RaceLog getRaceLog(String leaderboardName, Fleet fleet) {
		return raceLogStore.getRaceLog(fleet);
	}

    public RaceLog getRaceLog(Fleet fleet) {
		return getRaceLog("", fleet);
	}
}
