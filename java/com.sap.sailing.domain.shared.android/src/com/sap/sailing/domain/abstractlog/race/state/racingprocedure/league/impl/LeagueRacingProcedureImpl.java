package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.league.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.league.LeagueRacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.LeagueConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

// TODO consider subclassing RRS26RacingProcedureImpl instead of this copy&paste orgy...
public class LeagueRacingProcedureImpl extends BaseRacingProcedure implements LeagueRacingProcedure {
    private final static long startPhaseClassUpInterval = 3 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeUpInterval = 2 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseStartModeDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds

    private final static Flags startmodeFlag = Flags.PAPA;
    
    public LeagueRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author, 
             LeagueConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        update();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.LEAGUE;
    }
    
    @Override
    protected boolean hasIndividualRecallByDefault() {
        return true;
    }
    
    @Override
    protected Boolean isResultEntryEnabledByDefault() {
        return true;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
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

    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case RRS26_STARTMODE_UP:
        case RRS26_CLASS_UP:
        case RRS26_STARTMODE_DOWN:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        // Remark: We are reusing here CLASS_UP, STARTMODE_UP and STARTMODE_DOWN from the RRS26 procedure
        // The question is why there are no generic CLASS_UP, STARTMODE_UP and STARTMODE_DOWN events
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(startPhaseClassUpInterval), RaceStateEvents.RRS26_CLASS_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseStartModeUpInterval), RaceStateEvents.RRS26_STARTMODE_UP),
                new RaceStateEventImpl(startTime.minus(startPhaseStartModeDownInterval), RaceStateEvents.RRS26_STARTMODE_DOWN), 
                new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        Flags classFlag = getConfiguration().getClassFlag() != null ?
            getConfiguration().getClassFlag() : Flags.CLASS;
        if (now.before(startTime.minus(startPhaseClassUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(startmodeFlag, false)),
                    null,
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, false)), 
                    startTime.minus(startPhaseClassUpInterval));
        } else if (now.before(startTime.minus(startPhaseStartModeUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, false)),
                    startTime.minus(startPhaseClassUpInterval),
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, true)), 
                    startTime.minus(startPhaseStartModeUpInterval));
        } else if (now.before(startTime.minus(startPhaseStartModeDownInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, true)),
                    startTime.minus(startPhaseStartModeUpInterval),
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, false)), 
                    startTime.minus(startPhaseStartModeDownInterval));
        } else if (now.before(startTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(startmodeFlag, false)),
                    startTime.minus(startPhaseStartModeDownInterval),
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(startmodeFlag, false)), 
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
        return new LeagueRacingProcedureChangedListeners();
    }
}
