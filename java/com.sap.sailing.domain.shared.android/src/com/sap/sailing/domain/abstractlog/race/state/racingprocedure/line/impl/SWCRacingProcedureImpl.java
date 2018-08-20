package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import java.util.Arrays;
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
import com.sap.sailing.domain.common.SWCRacingProcedureConstants;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class SWCRacingProcedureImpl extends ConfigurableStartModeFlagRacingProcedureImpl implements SWCRacingProcedure {

    private final static Duration CLASS_AND_STARTMODE_UP_INTERVAL = Duration.ONE_MINUTE.times(6); // 6 minutes before start
    private final static Duration CLASS_AND_STARTMODE_DOWN_INTERVAL = Duration.ONE_MINUTE; // 1 minute after start
    private final static Duration FIVE_MINUTES_FLAG_UP_INTERVAL = Duration.ONE_MINUTE.times(5); // 5 minutes before start
    private final static Duration FOUR_MINUTES_FLAG_UP_INTERVAL = Duration.ONE_MINUTE.times(4); // 4 minutes before start
    private final static Duration THREE_MINUTES_FLAG_UP_INTERVAL = Duration.ONE_MINUTE.times(3); // 3 minutes before start
    private final static Duration TWO_MINUTES_FLAG_UP_INTERVAL = Duration.ONE_MINUTE.times(2); // 2 minutes before start
    private final static Duration ONE_MINUTE_FLAG_UP_INTERVAL = Duration.ONE_MINUTE; // 1 minutes before start

    public SWCRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author, 
             SWCStartConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
    }

    @Override
    protected Duration getStartPhaseStartModeUpInterval() {
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
            return cachedStartmodeFlag != Flags.BLACK && cachedStartmodeFlag != Flags.UNIFORM;
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
    public Iterable<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL), RaceStateEvents.SWC_CLASS_AND_STARTMODE_UP),
                new RaceStateEventImpl(startTime.minus(FIVE_MINUTES_FLAG_UP_INTERVAL), RaceStateEvents.SWC_FIVE_UP),
                new RaceStateEventImpl(startTime.minus(FOUR_MINUTES_FLAG_UP_INTERVAL), RaceStateEvents.SWC_FOUR_UP),
                new RaceStateEventImpl(startTime.minus(THREE_MINUTES_FLAG_UP_INTERVAL), RaceStateEvents.SWC_THREE_UP),
                new RaceStateEventImpl(startTime.minus(TWO_MINUTES_FLAG_UP_INTERVAL), RaceStateEvents.SWC_TWO_UP),
                new RaceStateEventImpl(startTime.minus(ONE_MINUTE_FLAG_UP_INTERVAL), RaceStateEvents.SWC_ONE_UP),
                new RaceStateEventImpl(startTime, RaceStateEvents.START),
                new RaceStateEventImpl(startTime, RaceStateEvents.SWC_GREEN_UP),
                new RaceStateEventImpl(startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL), RaceStateEvents.SWC_CLASS_AND_STARTMODE_DOWN));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case SWC_CLASS_AND_STARTMODE_UP:
            if (!startmodeFlagHasBeenSet()) {
                setStartModeFlag(event.getTimePoint(), cachedStartmodeFlag);
            }
        case SWC_CLASS_AND_STARTMODE_DOWN:
        case SWC_FIVE_UP:
        case SWC_FOUR_UP:
        case SWC_THREE_UP:
        case SWC_TWO_UP:
        case SWC_ONE_UP:
        case SWC_GREEN_UP:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        if (now.before(startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, false)),
                    null,
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true)), 
                    startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL));
        } else if (now.before(startTime.minus(FIVE_MINUTES_FLAG_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_FIVE, false)),
                    startTime.minus(CLASS_AND_STARTMODE_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_FIVE, true)), 
                    startTime.minus(FIVE_MINUTES_FLAG_UP_INTERVAL));
        } else if (now.before(startTime.minus(FOUR_MINUTES_FLAG_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_FIVE, true), new FlagPole(Flags.SWC_FOUR, false)),
                    startTime.minus(FIVE_MINUTES_FLAG_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_FOUR, true), new FlagPole(Flags.SWC_THREE, false)), 
                    startTime.minus(FOUR_MINUTES_FLAG_UP_INTERVAL));
        } else if (now.before(startTime.minus(THREE_MINUTES_FLAG_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_FOUR, true), new FlagPole(Flags.SWC_THREE, false)),
                    startTime.minus(FOUR_MINUTES_FLAG_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_THREE, true), new FlagPole(Flags.SWC_TWO, false)), 
                    startTime.minus(THREE_MINUTES_FLAG_UP_INTERVAL));
        } else if (now.before(startTime.minus(TWO_MINUTES_FLAG_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_THREE, true), new FlagPole(Flags.SWC_TWO, false)),
                    startTime.minus(THREE_MINUTES_FLAG_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_TWO, true), new FlagPole(Flags.SWC_ONE, false)), 
                    startTime.minus(TWO_MINUTES_FLAG_UP_INTERVAL));
        } else if (now.before(startTime.minus(ONE_MINUTE_FLAG_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_TWO, true), new FlagPole(Flags.SWC_ONE, false)),
                    startTime.minus(TWO_MINUTES_FLAG_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_ONE, true), new FlagPole(Flags.SWC_ZERO, false)), 
                    startTime.minus(ONE_MINUTE_FLAG_UP_INTERVAL));
        } else if (now.before(startTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_ONE, true), new FlagPole(Flags.SWC_ZERO, false)),
                    startTime.minus(ONE_MINUTE_FLAG_UP_INTERVAL),
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_ZERO, true)), 
                    startTime);
        } else if (now.before(startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, true), new FlagPole(Flags.SWC_ZERO, true)),
                    startTime,
                    Arrays.asList(new FlagPole(cachedStartmodeFlag, false), new FlagPole(Flags.SWC_ZERO, false)), 
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
                return new FlagPoleState(Collections.<FlagPole>emptyList(), recallRemoved == null ? startTime.plus(CLASS_AND_STARTMODE_DOWN_INTERVAL) : recallRemoved);
            }
        }
    }

    @Override
    public Flags getDefaultStartMode() {
        return SWCRacingProcedure.DEFAULT_START_MODE;
    }

    @Override
    public List<Flags> getDefaultStartModeFlags() {
        return SWCRacingProcedureConstants.DEFAULT_START_MODE_FLAGS;
    }

    @Override
    public SWCStartConfiguration getConfiguration() {
        return (SWCStartConfiguration) super.getConfiguration();
    }
}
