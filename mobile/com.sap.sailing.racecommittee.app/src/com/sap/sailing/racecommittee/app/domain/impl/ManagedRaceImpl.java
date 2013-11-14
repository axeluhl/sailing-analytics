package com.sap.sailing.racecommittee.app.domain.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import android.content.Context;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.SeriesBase;
import com.sap.sailing.domain.base.racegroup.RaceGroup;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.impl.RaceState2Impl;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.domain.ManagedRaceIdentifier;

public class ManagedRaceImpl implements ManagedRace {
    private static final long serialVersionUID = -4936566684992524001L;

    // private static final String TAG = ManagedRace.class.getName();

    private final RaceState2 state;
    private final ManagedRaceIdentifier identifier;
    private Collection<Competitor> competitors;
    private CourseBase courseOnServer;

    public ManagedRaceImpl(Context context, ManagedRaceIdentifier identifier,
            RacingProcedureType defaultStartProcedureType, RaceLog raceLog) {
        this(identifier, new RaceState2Impl(raceLog, AppPreferences.getAuthor(context), 
                RaceLogEventFactory.INSTANCE, defaultStartProcedureType));
    }

    public ManagedRaceImpl(ManagedRaceIdentifier identifier, RaceState2 state) {
        this.state = state;
        this.identifier = identifier;
        this.competitors = new ArrayList<Competitor>();
        this.courseOnServer = null;
    }

    @Override
    public RaceState2 getState2() {
        return state;
    }

    @Override
    public Serializable getId() {
        return identifier.getId();
    }

    @Override
    public String getName() {
        return identifier.getRaceName();
    }

    @Override
    public String getRaceName() {
        return getName();
    }

    @Override
    public Fleet getFleet() {
        return identifier.getFleet();
    }

    @Override
    public SeriesBase getSeries() {
        return identifier.getSeries();
    }

    @Override
    public RaceGroup getRaceGroup() {
        return identifier.getRaceGroup();
    }

    @Override
    public ManagedRaceIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public RaceLog getRaceLog() {
        return state.getRaceLog();
    }

    @Override
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
