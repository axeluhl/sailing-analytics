package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;

public class RaceListDataTypeHeader implements RaceListDataType {

    private BoatClassSeriesFleet data;
    private final static String DEFAULT = "Default";

    public RaceListDataTypeHeader(BoatClassSeriesFleet data) {
        this.data = data;
    }

    public BoatClassSeriesFleet getBoatClassSeriesDataFleet() {
        return data;
    }

    public BoatClass getBoatClass() {
        return data.getBoatClass();
    }

    public SeriesBase getSeries() {
        return data.getSeries();
    }

    public Fleet getFleet() {
        return data.getFleet();
    }

    public String toString() {
        String result = getBoatClass().getName();
        if (!getSeries().getName().equals(DEFAULT)) {
            result += " - " + getSeries().getName();
        }
        if (!getFleet().getName().equals(DEFAULT)) {
            result += " - " + getFleet().getName();
        }
        return result;
    }
}
