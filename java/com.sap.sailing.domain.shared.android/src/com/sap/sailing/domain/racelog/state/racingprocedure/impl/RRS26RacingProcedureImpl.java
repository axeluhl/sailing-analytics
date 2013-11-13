package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.RRS26StartModeFlagFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;

public class RRS26RacingProcedureImpl extends BaseRacingProcedure implements RRS26RacingProcedure {

    private final static long startPhaseClassUpInterval = 5 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds

    private final RRS26StartModeFlagFinder startmodeFlagAnalyzer;
    private Flags cachedStartmodeFlag;
    
    public RRS26RacingProcedureImpl(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory factory) {
        super(raceLog, author, factory);
        this.startmodeFlagAnalyzer = new RRS26StartModeFlagFinder(new RacingProcedureTypeAnalyzer(raceLog), raceLog);
        this.cachedStartmodeFlag = Flags.PAPA;
        
        update();
    }
    
    @Override
    protected RacingProcedureChangedListeners<?> createChangedListenerContainer() {
        return new RRS26ChangedListeners();
    }
    
    @Override
    protected RRS26ChangedListeners getChangedListeners() {
        return (RRS26ChangedListeners) super.getChangedListeners();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.RRS26;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime) {
        return null;
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        if (now.before(startTime)) {
            long timeTillStart = startTime.minus(now.asMillis()).asMillis();
            return timeTillStart < startPhaseClassUpInterval;
        }
        return false;
    }

    @Override
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(startPhaseClassUpInterval), RaceStateEvents.RRS26_CLASS_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseStartModeUpInterval), RaceStateEvents.RRS26_STARTMODE_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseStartModeDownInterval), RaceStateEvents.RRS26_STARTMODE_DOWN), 
                new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case RRS26_CLASS_UP:
        case RRS26_STARTMODE_UP:
        case RRS26_STARTMODE_DOWN:
        case START:
            // notify about change in shown flags
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public void addChangedListener(RRS26ChangedListener listener) {
        getChangedListeners().add(listener);
    }

    @Override
    public void removeChangedListener(RRS26ChangedListener listener) {
        getChangedListeners().remove(listener);
    }

    @Override
    public void setStartModeFlag(TimePoint timePoint, Flags startMode) {
        // TODO: create new event with start mode flag!
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), startMode, Flags.NONE, true));
    }

    @Override
    public Flags getStartModeFlag() {
        return cachedStartmodeFlag;
    }
    
    @Override
    protected void update() {
        Flags startmodeFlag = startmodeFlagAnalyzer.analyze();
        if (startmodeFlag != null && !startmodeFlag.equals(cachedStartmodeFlag)) {
            cachedStartmodeFlag = startmodeFlag;
            getChangedListeners().onStartmodeChanged(this);
        }
        super.update();
    }

}
