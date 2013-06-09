package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;

public class BoatClassSeriesFleet {

    private BoatClass boatClass;
    private SeriesBase series;
    private Fleet fleet;
    
    private static BoatClass getBoatClassForRace(ManagedRace managedRace) {
        if (managedRace.getRaceGroup().getBoatClass() == null) {
            return new BoatClassImpl(managedRace.getRaceGroup().getName(), false);
        }
        return managedRace.getRaceGroup().getBoatClass();
    }

    public BoatClassSeriesFleet(BoatClass boatClass, SeriesBase series, Fleet fleet) {
        this.boatClass = boatClass;
        this.series = series;
        this.fleet = fleet;
    }

    public BoatClassSeriesFleet(ManagedRace race) {
        this(getBoatClassForRace(race), race.getSeries(), race.getFleet());
    }

    public String getBoatClassName() {
        return boatClass.getName();
    }

    public String getFleetName() {
        return fleet.getName();
    }

    public String getSeriesName() {
        return series.getName();
    }

    public BoatClass getBoatClass() {
        return boatClass;
    }

    public SeriesBase getSeries() {
        return series;
    }

    public Fleet getFleet() {
        return fleet;
    }

    @Override
    public boolean equals(Object obj) {
        BoatClassSeriesFleet other = (BoatClassSeriesFleet) obj;
        return getBoatClassName().equals(other.getBoatClassName()) && getSeriesName().equals(other.getSeriesName())
                && getFleetName().equals(other.getFleetName());
    }

    @Override
    public int hashCode() {
        // / TODO: Check implementation of equals/hashCode on BoatGroupAndSeries
        return 123;
    }
}
