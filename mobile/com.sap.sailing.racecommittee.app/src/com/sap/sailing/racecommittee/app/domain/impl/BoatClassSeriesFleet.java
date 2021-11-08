package com.sap.sailing.racecommittee.app.domain.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sse.common.Util;

public class BoatClassSeriesFleet {

    private BoatClass boatClass;
    private SeriesBase series;
    private int seriesOrder;
    private int fleetOrder;
    private Fleet fleet;

    private static BoatClass getBoatClassForRace(ManagedRace managedRace) {
        if (managedRace.getRaceGroup().getBoatClass() == null) {
            return new BoatClassImpl(managedRace.getRaceGroup().getName(), false);
        }
        return managedRace.getRaceGroup().getBoatClass();
    }

    private static int getSeriesIndex(ManagedRace race, SeriesBase series) {
        return Util.indexOf(race.getRaceGroup().getSeries(), series);
    }

    public BoatClassSeriesFleet(ManagedRace race) {
        this.boatClass = getBoatClassForRace(race);
        this.series = race.getSeries();
        this.seriesOrder = getSeriesIndex(race, series);
        this.fleetOrder = getFleetIndex(series.getFleets(), race.getFleet());
        this.fleet = race.getFleet();
    }

    private int getFleetIndex(Iterable<? extends Fleet> fleets, Fleet fleet) {
        return Util.indexOf(fleets, fleet);
    }

    public String getBoatClassName() {
        return boatClass.getName();
    }

    public String getFleetName() {
        return fleet.getName();
    }

    public int getFleetOrdering() {
        return fleet.getOrdering();
    }

    public String getSeriesName() {
        return series.getName();
    }

    public int getSeriesOrder() {
        return seriesOrder;
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

    private final static String DEFAULT = "Default";

    public String getDisplayName() {
        String result = getBoatClass().getName();
        if (!getSeries().getName().equals(DEFAULT)) {
            result += " - " + getSeries().getName();
        }
        if (!getFleet().getName().equals(DEFAULT)) {
            result += " - " + getFleet().getName();
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((boatClass == null) ? 0 : boatClass.hashCode());
        result = prime * result + ((fleet == null) ? 0 : fleet.hashCode());
        result = prime * result + ((series == null) ? 0 : series.hashCode());
        result = prime * result + seriesOrder;
        result = prime * result + fleetOrder;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BoatClassSeriesFleet other = (BoatClassSeriesFleet) obj;
        if (boatClass == null) {
            if (other.boatClass != null)
                return false;
        } else if (!boatClass.equals(other.boatClass))
            return false;
        if (fleet == null) {
            if (other.fleet != null)
                return false;
        } else if (!fleet.equals(other.fleet))
            return false;
        if (series == null) {
            if (other.series != null)
                return false;
        } else if (!series.equals(other.series))
            return false;
        if (seriesOrder != other.seriesOrder)
            return false;
        if (fleetOrder != other.fleetOrder)
            return false;
        return true;
    }

    public int getFleetOrder() {
        return fleetOrder;
    }

}
