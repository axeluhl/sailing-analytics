package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.base.racegroup.SeriesWithRows;

public interface FleetIdentifier {

    public Fleet getFleet();

    public SeriesWithRows getSeries();

    public RaceGroup getRaceGroup();

    public String getId();
}
