package com.sap.sailing.domain.base.racegroup.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.racegroup.RaceCell;
import com.sap.sailing.domain.common.impl.NamedImpl;
import com.sap.sailing.domain.racelog.RaceLog;

public class RaceCellImpl extends NamedImpl implements RaceCell {
    private static final long serialVersionUID = 971598420407273594L;

    private RaceLog raceLog;
    private Iterable<Competitor> competitors;

    public RaceCellImpl(String name, RaceLog raceLog, Iterable<Competitor> competitors) {
        super(name);
        this.raceLog = raceLog;
        this.competitors = competitors;
    }

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    @Override
    public Iterable<Competitor> getCompetitors() {
        return competitors;
    }

}
