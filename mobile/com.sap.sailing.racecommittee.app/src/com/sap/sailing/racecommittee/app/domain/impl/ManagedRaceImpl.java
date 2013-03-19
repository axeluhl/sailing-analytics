package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;

import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesData;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.impl.RaceStateImpl;

public class ManagedRaceImpl implements ManagedRace {
    private static final long serialVersionUID = -4936566684992524001L;

    //private static final String TAG = ManagedRace.class.getName();

    private ManagedRaceIdentifier identifier;	
    private RaceState state;

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, PassAwareRaceLog raceLog) {
        this(identifier, new RaceStateImpl(raceLog, null));
    }

    public ManagedRaceImpl(
            ManagedRaceIdentifier identifier,
            RaceState state) {
        this.identifier = identifier;
        this.state = state;
    }

    public Serializable getId() {
        return identifier.getId();
    }

    public String getName() {
        return identifier.getRaceName();
    }

    public String getRaceName() {
        return getName();
    }

    public Fleet getFleet() {
        return identifier.getFleet();
    }

    public SeriesData getSeries() {
        return identifier.getSeries();
    }

    public RaceGroup getRaceGroup() {
        return identifier.getRaceGroup();
    }

    public ManagedRaceIdentifier getIdentifier() {
        return identifier;
    }

    public RaceState getState() {
        return state;
    }

    public RaceLog getRaceLog() {
        return state.getRaceLog();
    }

    public RaceLogRaceStatus getStatus() {
        return state.getStatus();
    }

    @Override
    public CourseData getCourseDesign() {
        return state.getCourseDesign();
    }

}
