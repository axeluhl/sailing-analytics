package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;
import com.sap.sailing.domain.racelog.state.racingprocedure.ESSChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.ESSRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;

public class ESSRacingProcedureImpl extends BaseRacingProcedure implements ESSRacingProcedure {

    private final static long startPhaseAPDownInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSThreeUpInterval = 3 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSTwoUpInterval = 2 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneUpInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    
    public ESSRacingProcedureImpl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory factory) {
        super(raceLog, author, factory);
        update();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.ESS;
    }
    
    @Override
    public boolean hasIndividualRecall() {
        return true;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime, TimePoint now) {
        return null;
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        if (now.before(startTime)) {
            long timeTillStart = startTime.minus(now.asMillis()).asMillis();
            return timeTillStart < startPhaseAPDownInterval;
        }
        return false;
    }

    @Override
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(startPhaseAPDownInterval), RaceStateEvents.ESS_AP_DOWN),
                new RaceStateEventImpl(startTime.minus(startPhaseESSThreeUpInterval), RaceStateEvents.ESS_THREE_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseESSTwoUpInterval), RaceStateEvents.ESS_TWO_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseESSOneUpInterval), RaceStateEvents.ESS_ONE_UP),
                new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case ESS_AP_DOWN:
        case ESS_THREE_UP:
        case ESS_TWO_UP:
        case ESS_ONE_UP:
        case START:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        if (now.before(startTime.minus(startPhaseAPDownInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.AP, true), new FlagPole(Flags.ESSTHREE, false)), 
                    Arrays.asList(new FlagPole(Flags.AP, false), new FlagPole(Flags.ESSTHREE, false)), 
                    startTime.minus(startPhaseAPDownInterval));
        } else if (now.before(startTime.minus(startPhaseESSThreeUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.AP, false), new FlagPole(Flags.ESSTHREE, false)), 
                    Arrays.asList(new FlagPole(Flags.ESSTHREE, true), new FlagPole(Flags.ESSTWO, false)), 
                    startTime.minus(startPhaseESSThreeUpInterval));
        } else if (now.before(startTime.minus(startPhaseESSTwoUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.ESSTHREE, true), new FlagPole(Flags.ESSTWO, false)), 
                    Arrays.asList(new FlagPole(Flags.ESSTWO, true), new FlagPole(Flags.ESSONE, false)), 
                    startTime.minus(startPhaseESSTwoUpInterval));
        } else if (now.before(startTime.minus(startPhaseESSOneUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.ESSTWO, true), new FlagPole(Flags.ESSONE, false)), 
                    Arrays.asList(new FlagPole(Flags.ESSONE, true)), 
                    startTime.minus(startPhaseESSOneUpInterval));
        } if (now.before(startTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.ESSONE, true)), 
                    Arrays.asList(new FlagPole(Flags.ESSONE, false)), 
                    startTime);
        } else {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.ESSONE, false)));
        }
    }
    
    @Override
    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer() {
        return new ESSChangedListeners();
    }
    
    @Override
    protected ESSChangedListeners getChangedListeners() {
        return (ESSChangedListeners) super.getChangedListeners();
    }

    @Override
    public void addChangedListener(ESSChangedListener listener) {
        getChangedListeners().add(listener);
    }
    
    @Override
    public TimePoint getTimeLimit(TimePoint startTime) {
        TimePoint firstBoatTime = new FinishingTimeFinder(raceLog).analyze();
        if (firstBoatTime != null) {
            return firstBoatTime.plus((long) ((firstBoatTime.asMillis() - startTime.asMillis()) * 0.75));
        }
        return null;
    }

}
