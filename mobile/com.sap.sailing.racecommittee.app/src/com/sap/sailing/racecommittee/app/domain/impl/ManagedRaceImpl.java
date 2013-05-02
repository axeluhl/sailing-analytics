package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.impl.RaceStateImpl;

public class ManagedRaceImpl implements ManagedRace {
    private static final long serialVersionUID = -4936566684992524001L;

    // private static final String TAG = ManagedRace.class.getName();

    private ManagedRaceIdentifier identifier;
    private RaceState state;
    private Collection<Competitor> competitors;
    private CourseBase courseOnServer;

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, StartProcedureType defaultStartProcedureType,
            PassAwareRaceLog raceLog) {
        this(identifier, new RaceStateImpl(defaultStartProcedureType, raceLog));
    }

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, RaceState state) {
        this.identifier = identifier;
        this.state = state;
        this.competitors = new ArrayList<Competitor>();
        this.courseOnServer = null;
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

    public SeriesBase getSeries() {
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
    public CourseBase getCourseDesign() {
        return state.getCourseDesign();
    }

    @Override
    public Collection<Competitor> getCompetitors() {
        return competitors;
    }

    @Override
    public CourseBase getCourseOnServer() {
        return courseOnServer;
    }

    @Override
    public void setCourseOnServer(CourseBase course) {
        courseOnServer = course;
    }

    @Override
    public void setCompetitors(Collection<Competitor> competitors) {
        this.competitors = competitors;
    }

}
