package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.SWCRacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

public class SWCRacingProcedureImpl extends ConfigurableStartModeFlagRacingProcedureImpl implements SWCRacingProcedure {

    private final static long CLASS_AND_STARTMODE_UP_INTERVAL = 6 * 60 * 1000; // 6 minutes before start
    private final static long CLASS_AND_STARTMODE_DOWN_INTERVAL = 1 * 60 * 1000; // 1 minute after start

    public SWCRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author, 
             SWCStartConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
    }

    @Override
    protected long getStartPhaseStartModeUpInterval() {
        return CLASS_AND_STARTMODE_UP_INTERVAL;
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.SWC;
    }
    
    @Override
    public boolean hasIndividualRecall() {
        boolean hasRecall = super.hasIndividualRecall();
        if (!hasRecall) {
            return false;
        } else if (startmodeFlagHasBeenSet()) {
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
    protected Boolean isResultEntryEnabledByDefault() {
        return false;
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        return now.before(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL));
    }

    @Override
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL), RaceStateEvents.SWC_CLASS_UP),
                new RaceStateEventImpl(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL), RaceStateEvents.SWC_STARTMODE_UP),
                new RaceStateEventImpl(startTime, RaceStateEvents.START),
                new RaceStateEventImpl(startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL), RaceStateEvents.SWC_STARTMODE_DOWN), 
                new RaceStateEventImpl(startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL), RaceStateEvents.SWC_CLASS_DOWN));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case SWC_STARTMODE_UP:
            if (!startmodeFlagHasBeenSet()) {
                setStartModeFlag(event.getTimePoint(), cachedStartmodeFlag);
            }
        case SWC_CLASS_UP:
        case SWC_STARTMODE_DOWN:
        case SWC_CLASS_DOWN:
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
            
        if (now.before(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(cachedStartmodeFlag, false)),
                    null,
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, true)), 
                    startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL));
        } else if (now.before(startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, true), new FlagPole(cachedStartmodeFlag, true)),
                    startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL),
                    Arrays.asList(new FlagPole(classFlag, false), new FlagPole(cachedStartmodeFlag, false)), 
                    startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL));
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
    public Flags getDefaultStartMode() {
        return SWCRacingProcedure.DEFAULT_START_MODE;
    }

    @Override
    public List<Flags> getDefaultStartModeFlags() {
        return SWCRacingProcedure.DEFAULT_START_MODE_FLAGS;
    }

    @Override
    public SWCStartConfiguration getConfiguration() {
        return (SWCStartConfiguration) super.getConfiguration();
    }
}
