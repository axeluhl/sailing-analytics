package com.sap.sailing.racecommittee.app.domain;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;

import java.io.Serializable;

public interface FleetIdentifier {

    public Fleet getFleet();

    public SeriesBase getSeries();

    public RaceGroup getRaceGroup();

    public Serializable getId();
}
