package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.racecommittee.app.domain.impl.BoatClassSeriesFleet;

public class RaceListDataTypeHeader implements RaceListDataType {

    private BoatClassSeriesFleet data;

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

    @Override
    public String toString() {
        return data.getDisplayName();
    }
}
