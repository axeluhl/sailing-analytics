package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.FilterableRace;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;

public class SimpleFilterableRace implements FilterableRace {
    private final RaceGroup raceGroup;
    private final SeriesBase series;
    private final Fleet fleet;
    private RaceLogRaceStatus status;
    private final int zeroBasedIndexInFleet;
    private final int zeroBasedSeriesIndex;
    private final String raceColumnName;
    
    public SimpleFilterableRace(RaceGroup raceGroup, SeriesBase series, Fleet fleet, int zeroBasedIndexInFleet,
            int zeroBasedSeriesIndex, String raceColumnName) {
        super();
        this.raceGroup = raceGroup;
        this.series = series;
        this.fleet = fleet;
        this.zeroBasedIndexInFleet = zeroBasedIndexInFleet;
        this.zeroBasedSeriesIndex = zeroBasedSeriesIndex;
        this.raceColumnName = raceColumnName;
        this.status = RaceLogRaceStatus.UNSCHEDULED;
    }

    @Override
    public RaceGroup getRaceGroup() {
        return raceGroup;
    }

    @Override
    public SeriesBase getSeries() {
        return series;
    }

    @Override
    public Fleet getFleet() {
        return fleet;
    }

    @Override
    public RaceLogRaceStatus getStatus() {
        return status;
    }
    
    public void setStatus(RaceLogRaceStatus status) {
        this.status = status;
    }

    @Override
    public int getZeroBasedIndexInFleet() {
        return zeroBasedIndexInFleet;
    }

    @Override
    public int getZeroBasedSeriesIndex() {
        return zeroBasedSeriesIndex;
    }

    @Override
    public String getRaceColumnName() {
        return raceColumnName;
    }

    @Override
    public String toString() {
        return "SimpleFilterableRace ["+getRaceGroup().getName() + " - " + getSeries().getName() + " - " +
                getFleet().getName() + " - " + getRaceColumnName() + "]";
    }

}
