package com.sap.sailing.domain.leaderboard.impl;

import java.util.Collections;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.impl.AbstractRaceColumn;
import com.sap.sailing.domain.leaderboard.FlexibleRaceColumn;
import com.sap.sailing.domain.tracking.RaceExecutionOrderProvider;

public class RaceColumnImpl extends AbstractRaceColumn implements FlexibleRaceColumn {
    private static final long serialVersionUID = -7801617988982540470L;
    private String name;

    private boolean medalRace;

    public RaceColumnImpl(String name, boolean medalRace) {
        super();
        this.name = name;
        this.medalRace = medalRace;
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
    public Iterable<? extends Fleet> getFleets() {
        return Collections.singleton(FlexibleLeaderboardImpl.defaultFleet);
    }

    @Override
    public RaceExecutionOrderProvider getRaceExecutionOrderProvider() {
        return null;
    }

}
