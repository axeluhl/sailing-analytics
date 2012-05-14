package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Series;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.leaderboard.RaceColumn;

public class SeriesImpl extends NamedImpl implements Series {
    private static final long serialVersionUID = -1640404303144907381L;
    private final boolean isFleetsOrdered;
    private final Iterable<? extends Fleet> fleets;
    private final Iterable<? extends RaceColumn> raceColumns;
    
    public SeriesImpl(String name, boolean isFleetsOrdered, Iterable<? extends Fleet> fleets, Iterable<? extends RaceColumn> raceColumns) {
        super(name);
        this.isFleetsOrdered = isFleetsOrdered;
        this.fleets = fleets;
        this.raceColumns = raceColumns;
    }

    public boolean isFleetsOrdered() {
        return isFleetsOrdered;
    }

    public Iterable<? extends Fleet> getFleets() {
        return fleets;
    }

    @Override
    public Iterable<? extends RaceColumn> getRaceColumns() {
        return raceColumns;
    }

}
