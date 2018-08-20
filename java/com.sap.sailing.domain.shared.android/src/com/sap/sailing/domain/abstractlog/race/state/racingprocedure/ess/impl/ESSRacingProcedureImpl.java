package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.impl;

import java.util.Arrays;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.base.configuration.procedures.ESSConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

public class ESSRacingProcedureImpl extends BaseRacingProcedure implements ESSRacingProcedure {

    private final static long startPhaseAPDownInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSThreeUpInterval = 3 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSTwoUpInterval = 2 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneUpInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds

    @SuppressWarnings("unused")
    private final ESSConfiguration configuration;

    public ESSRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author, ESSConfiguration configuration,
            RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        this.configuration = configuration;
        update();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.ESS;
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
            return timeTillStart < startPhaseAPDownInterval;
        }
        return false;
    }

    @Override
    public Iterable<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(new RaceStateEventImpl(startTime.minus(startPhaseAPDownInterval),
                RaceStateEvents.ESS_AP_DOWN), new RaceStateEventImpl(startTime.minus(startPhaseESSThreeUpInterval),
                RaceStateEvents.ESS_THREE_UP), new RaceStateEventImpl(startTime.minus(startPhaseESSTwoUpInterval),
                RaceStateEvents.ESS_TWO_UP), new RaceStateEventImpl(startTime.minus(startPhaseESSOneUpInterval),
                RaceStateEvents.ESS_ONE_UP), new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case ESS_AP_DOWN:
        case ESS_THREE_UP:
        case ESS_TWO_UP:
        case ESS_ONE_UP:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        if (now.before(startTime.minus(startPhaseAPDownInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(Flags.AP, true), new FlagPole(Flags.ESSTHREE, false)),
                    null, Arrays.asList(new FlagPole(Flags.AP, false), new FlagPole(Flags.ESSTHREE, false)),
                    startTime.minus(startPhaseAPDownInterval));
        } else if (now.before(startTime.minus(startPhaseESSThreeUpInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(Flags.AP, false), new FlagPole(Flags.ESSTHREE, false)),
                    startTime.minus(startPhaseAPDownInterval), Arrays.asList(new FlagPole(Flags.ESSTHREE, true),
                            new FlagPole(Flags.ESSTWO, false)), startTime.minus(startPhaseESSThreeUpInterval));
        } else if (now.before(startTime.minus(startPhaseESSTwoUpInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(Flags.ESSTHREE, true),
                    new FlagPole(Flags.ESSTWO, false)), startTime.minus(startPhaseESSThreeUpInterval), Arrays.asList(
                    new FlagPole(Flags.ESSTWO, true), new FlagPole(Flags.ESSONE, false)),
                    startTime.minus(startPhaseESSTwoUpInterval));
        } else if (now.before(startTime.minus(startPhaseESSOneUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.ESSTWO, true), new FlagPole(Flags.ESSONE, false)),
                    startTime.minus(startPhaseESSTwoUpInterval), Arrays.asList(new FlagPole(Flags.ESSONE, true)),
                    startTime.minus(startPhaseESSOneUpInterval));
        }
        if (now.before(startTime)) {
            return new FlagPoleState(Arrays.asList(new FlagPole(Flags.ESSONE, true)),
                    startTime.minus(startPhaseESSOneUpInterval), Arrays.asList(new FlagPole(Flags.ESSONE, false)),
                    startTime);
        } else {
            if (isIndividualRecallDisplayed(now)) {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.XRAY, true)),
                        getIndividualRecallDisplayedTime(), Arrays.asList(new FlagPole(Flags.XRAY, false)),
                        getIndividualRecallRemovalTime());
            } else if (isFinished(now)) {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.BLUE, false)), getFinishedTime());
            } else if (isInFinishingPhase(now)) {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.BLUE, true)), getFinishingTime(),
                        Arrays.asList(new FlagPole(Flags.BLUE, false)), null);
            } else {
                TimePoint recallRemoved = getIndividualRecallRemovalTime();
                return new FlagPoleState(Collections.<FlagPole> emptyList(), recallRemoved == null ? startTime
                        : recallRemoved);
            }
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

    @Override
    protected boolean hasIndividualRecallByDefault() {
        return true;
    }

    @Override
    protected Boolean isResultEntryEnabledByDefault() {
        return true;
    }

    @Override
    public ESSConfiguration getConfiguration() {
        return (ESSConfiguration) super.getConfiguration();
    }

}
