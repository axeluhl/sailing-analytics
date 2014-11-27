package com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.FlagPole;
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
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sse.common.TimePoint;

public class RRS26RacingProcedureImpl extends BaseRacingProcedure implements RRS26RacingProcedure {

    private final static long startPhaseClassUpInterval = 5 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds

    private final RRS26StartModeFlagFinder startModeFlagAnalyzer;
    
    private Flags cachedStartmodeFlag;
    private boolean startmodeFlagHasBeenSet;

    public RRS26RacingProcedureImpl(RaceLog raceLog, RaceLogEventAuthor author, 
            RaceLogEventFactory factory, RRS26Configuration configuration) {
        super(raceLog, author, factory, configuration);
        
        RacingProcedureTypeAnalyzer procedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        if (configuration.getStartModeFlags() != null) {
            this.startModeFlagAnalyzer = new RRS26StartModeFlagFinder(procedureAnalyzer, raceLog, configuration.getStartModeFlags());
        } else {
            this.startModeFlagAnalyzer = new RRS26StartModeFlagFinder(procedureAnalyzer, raceLog);
        }
        
        this.cachedStartmodeFlag = RRS26RacingProcedure.DefaultStartMode;
        this.startmodeFlagHasBeenSet = false;
        
        update();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.RRS26;
    }
    
    @Override
    public boolean hasIndividualRecall() {
        boolean hasRecall = super.hasIndividualRecall();
        if (!hasRecall) {
            return false;
        } else if (startmodeFlagHasBeenSet) {
            return cachedStartmodeFlag != Flags.BLACK;
        } else {
            return hasRecall;
        }
    }
    
    @Override
    protected boolean hasIndividualRecallByDefault() {
        return true;
    }
    
    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
        if (startTime.minus(startPhaseStartModeUpInterval).before(now) && !startmodeFlagHasBeenSet) {
            return new StartmodePrerequisite(function, this, now, startTime);
        }
        return new NoMorePrerequisite(function);
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
        case RRS26_STARTMODE_UP:
            if (!startmodeFlagHasBeenSet) {
                setStartModeFlag(event.getTimePoint(), cachedStartmodeFlag);
            }
        case RRS26_CLASS_UP:
        case RRS26_STARTMODE_DOWN:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        Flags classFlag = getConfiguration().getClassFlag() != null ?
            getConfiguration().getClassFlag() : Flags.CLASS;
        if (now.before(startTime.minus(startPhaseClassUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(cachedStartmodeFlag, false)),
                    null,
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, false)), 
                    startTime.minus(startPhaseClassUpInterval));
        } else if (now.before(startTime.minus(startPhaseStartModeUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, false)),
                    startTime.minus(startPhaseClassUpInterval),
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, true)), 
                    startTime.minus(startPhaseStartModeUpInterval));
        } else if (now.before(startTime.minus(startPhaseStartModeDownInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, true)),
                    startTime.minus(startPhaseStartModeUpInterval),
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, false)), 
                    startTime.minus(startPhaseStartModeDownInterval));
        } else if (now.before(startTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, false)),
                    startTime.minus(startPhaseStartModeDownInterval),
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(cachedStartmodeFlag, false)), 
                    startTime);
        } else {
            if (isIndividualRecallDisplayed(now)) {
                return new FlagPoleState(
                        Arrays.asList(new FlagPole(Flags.XRAY, true)),
                        getIndividualRecallDisplayedTime(),
                        Arrays.asList(new FlagPole(Flags.XRAY, false)),
                        getIndividualRecallRemovalTime());
            } else if (isFinished(now)) {
                return new FlagPoleState(
                        Arrays.asList(new FlagPole(Flags.BLUE, false)), getFinishedTime());
            } else if (isInFinishingPhase(now)) {
                return new FlagPoleState(
                        Arrays.asList(new FlagPole(Flags.BLUE, true)),
                        getFinishingTime(),
                        Arrays.asList(new FlagPole(Flags.BLUE, false)),
                        null);
            } else {
                TimePoint recallRemoved = getIndividualRecallRemovalTime();
                return new FlagPoleState(Collections.<FlagPole>emptyList(), recallRemoved == null ? startTime : recallRemoved);
            }
        }
    }
    
    @Override
    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer() {
        return new RRS26ChangedListeners();
    }
    
    @Override
    protected RRS26ChangedListeners getChangedListeners() {
        return (RRS26ChangedListeners) super.getChangedListeners();
    }

    @Override
    public void addChangedListener(RRS26ChangedListener listener) {
        getChangedListeners().add(listener);
    }

    @Override
    public void setStartModeFlag(TimePoint timePoint, Flags startMode) {
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), startMode, Flags.NONE, true));
    }

    @Override
    public Flags getStartModeFlag() {
        return cachedStartmodeFlag;
    }
    
    public boolean startmodeFlagHasBeenSet() {
        return startmodeFlagHasBeenSet;
    }
    
    @Override
    protected void update() {
        Flags startmodeFlag = startModeFlagAnalyzer.analyze();
        if (startmodeFlag != null && (!startmodeFlag.equals(cachedStartmodeFlag) || !startmodeFlagHasBeenSet)) {
            cachedStartmodeFlag = startmodeFlag;
            startmodeFlagHasBeenSet = true;
            getChangedListeners().onStartmodeChanged(this);
        }
        super.update();
    }
    
    @Override
    public RRS26Configuration getConfiguration() {
        return (RRS26Configuration) super.getConfiguration();
    }

}
