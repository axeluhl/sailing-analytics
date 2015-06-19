package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;

public class RaceListDataTypeHeader implements RaceListDataType {

    private RaceGroupSeriesFleet data;

    public RaceListDataTypeHeader(RaceGroupSeriesFleet data) {
        this.data = data;
    }

    public RaceGroupSeriesFleet getRegattaSeriesFleet() {
        return data;
    }

    public RaceGroup getRaceGroup() {
        return data.getRaceGroup();
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
