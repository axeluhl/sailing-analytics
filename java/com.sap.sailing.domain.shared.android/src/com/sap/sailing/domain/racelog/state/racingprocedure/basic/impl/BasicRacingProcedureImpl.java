package com.sap.sailing.domain.racelog.state.racingprocedure.basic.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sse.common.TimePoint;

public class BasicRacingProcedureImpl extends BaseRacingProcedure {

    public BasicRacingProcedureImpl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory factory,
            RacingProcedureConfiguration configuration) {
        super(raceLog, author, factory, configuration);
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.BASIC;
    }
    
    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
        return new NoMorePrerequisite(function);
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        return now.before(startTime);
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        return new FlagPoleState(Collections.<FlagPole>emptyList(), null);
    }

    @Override
    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer() {
        return new RacingProcedureChangedListeners<RacingProcedureChangedListener>();
    }

    @Override
    protected boolean hasIndividualRecallByDefault() {
        return false;
    }

    @Override
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent>asList(new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

}
