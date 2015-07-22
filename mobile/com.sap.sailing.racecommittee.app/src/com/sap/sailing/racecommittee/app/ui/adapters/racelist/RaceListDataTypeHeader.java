package com.sap.sailing.racecommittee.app.ui.adapters.racelist;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.racecommittee.app.domain.impl.RaceGroupSeriesFleet;

public class RaceListDataTypeHeader implements RaceListDataType {

    private RaceGroupSeriesFleet data;
    private boolean isFleetVisible;

    public RaceListDataTypeHeader(RaceGroupSeriesFleet data) {
        this(data, true);
    }

    public RaceListDataTypeHeader(RaceGroupSeriesFleet data, boolean isFleetVisible) {
        this.data = data;
        this.isFleetVisible = isFleetVisible;
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

    public boolean isFleetVisible() { return isFleetVisible; }

    @Override
    public String toString() {
        return data.getDisplayName();
    }
}
