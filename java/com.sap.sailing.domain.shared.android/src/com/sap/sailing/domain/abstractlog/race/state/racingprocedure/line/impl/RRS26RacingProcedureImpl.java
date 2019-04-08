package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.RRS26RacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RRS26RacingProcedureImpl extends ConfigurableStartModeFlagRacingProcedureImpl implements RRS26RacingProcedure {

    private final static Duration startPhaseClassUpInterval = Duration.ONE_MINUTE.times(5);
    private final static Duration startPhaseStartModeUpInterval = Duration.ONE_MINUTE.times(4);
    private final static Duration startPhaseStartModeDownInterval = Duration.ONE_MINUTE;

    public RRS26RacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
                                    RRS26Configuration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
    }

    @Override
    protected Duration getStartPhaseStartModeUpInterval() {
        return startPhaseStartModeUpInterval;
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.RRS26;
    }

    @Override
    public boolean hasIndividualRecall() {
        boolean hasRecall = super.hasIndividualRecall();
        if (hasRecall) {
            return true;
        }
        if (startmodeFlagHasBeenSet) {
            return cachedStartmodeFlag != Flags.BLACK;
        }
        return false;
    }

    @Override
    protected boolean hasIndividualRecallByDefault() {
        return true;
    }

    @Override
    protected Boolean isResultEntryEnabledByDefault() {
        return false;
    }

    @Override
    public Iterable<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent>asList(
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
                        Collections.singletonList(new FlagPole(Flags.XRAY, true)),
                        getIndividualRecallDisplayedTime(),
                        Collections.singletonList(new FlagPole(Flags.XRAY, false)),
                        getIndividualRecallRemovalTime());
            } else if (isFinished(now)) {
                return new FlagPoleState(
                        Collections.singletonList(new FlagPole(Flags.BLUE, false)), getFinishedTime());
            } else if (isInFinishingPhase(now)) {
                return new FlagPoleState(
                        Collections.singletonList(new FlagPole(Flags.BLUE, true)),
                        getFinishingTime(),
                        Collections.singletonList(new FlagPole(Flags.BLUE, false)),
                        null);
            } else {
                TimePoint recallRemoved = getIndividualRecallRemovalTime();
                return new FlagPoleState(Collections.<FlagPole>emptyList(), recallRemoved == null ? startTime : recallRemoved);
            }
        }
    }

    @Override
    public Flags getDefaultStartMode() {
        return RRS26RacingProcedure.DEFAULT_START_MODE;
    }

    @Override
    public List<Flags> getDefaultStartModeFlags() {
        return RRS26RacingProcedure.DEFAULT_START_MODE_FLAGS;
    }

    @Override
    public RRS26Configuration getConfiguration() {
        return (RRS26Configuration) super.getConfiguration();
    }
}
