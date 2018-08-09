package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.GateLineOpeningTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.PathfinderFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogGateLineOpeningTimeEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogPathfinderEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedureChangedListeners;
import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class GateStartRacingProcedureImpl extends BaseRacingProcedure implements GateStartRacingProcedure {

    private final static long startPhaseClassOverGolfUpInterval = 8 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds

    private final GateLineOpeningTimeFinder gateLineOpeningTimeAnalyzer;
    private final PathfinderFinder pathfinderAnalyzer;;

    private GateLineOpeningTimes cachedGateLineOpeningTimes;
    private boolean gateLineOpeningTimesHasBeenSet;
    private String cachedPathfinder;

    public GateStartRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author,
            GateStartConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        this.gateLineOpeningTimeAnalyzer = new GateLineOpeningTimeFinder(raceLog);
        this.pathfinderAnalyzer = new PathfinderFinder(raceLog);

        long defaultGolfDownTime = getDefaultGolfDownTime();
        this.cachedGateLineOpeningTimes = new GateLineOpeningTimes(GateStartRacingProcedure.DefaultGateLaunchStopTime,
                defaultGolfDownTime);
        this.gateLineOpeningTimesHasBeenSet = false;

        update();
    }

    protected long getDefaultGolfDownTime() {
        Boolean flag = getConfiguration().hasAdditionalGolfDownTime();
        if (flag != null) {
            return flag ? GateStartRacingProcedure.DefaultGolfDownTime : 0;
        }
        return 0;
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.GateStart;
    }

    @Override
    protected boolean hasIndividualRecallByDefault() {
        return false;
    }

    @Override
    protected Boolean isResultEntryEnabledByDefault() {
        return false;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
        if (startTime.before(now)) {
            if (getConfiguration().hasPathfinder() != null && getConfiguration().hasPathfinder()
                    && getPathfinder() == null) {
                return new PathfinderPrerequisite(function, this, now, startTime);
            } else if (!gateLineOpeningTimesHasBeenSet) {
                return new GateLaunchTimePrerequisite(function, this, now, startTime, getDefaultGolfDownTime());
            }
        }
        return new NoMorePrerequisite(function);
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        if (now.before(startTime)) {
            long timeTillStart = startTime.minus(now.asMillis()).asMillis();
            return timeTillStart < startPhaseClassOverGolfUpInterval;
        }
        return false;
    }

    @Override
    public void triggerStateEventScheduling(ReadonlyRaceState state) {
        switch (state.getStatus()) {
        case SCHEDULED:
        case STARTPHASE:
        case RUNNING:
            rescheduleGateShutdownTime(state.getStartTime());
            break;
        default:
            break;
        }
        super.triggerStateEventScheduling(state);
    }

    @Override
    public Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(startPhaseClassOverGolfUpInterval),
                        RaceStateEvents.GATE_CLASS_OVER_GOLF_UP),
                new RaceStateEventImpl(startTime.minus(startPhasePapaUpInterval), RaceStateEvents.GATE_PAPA_UP),
                new RaceStateEventImpl(startTime.minus(startPhasePapaDownInterval), RaceStateEvents.GATE_PAPA_DOWN),
                new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case START:
            if (!gateLineOpeningTimesHasBeenSet) {
                setGateLineOpeningTimes(event.getTimePoint(), cachedGateLineOpeningTimes.getGateLaunchStopTime(),
                        cachedGateLineOpeningTimes.getGolfDownTime());
            }
            rescheduleGateShutdownTime(event.getTimePoint());
            return true;
        case GATE_CLASS_OVER_GOLF_UP:
        case GATE_PAPA_UP:
        case GATE_PAPA_DOWN:
        case GATE_SHUTDOWN:
            getChangedListeners().onActiveFlagsChanged(this);
            return true;
        default:
            return super.processStateEvent(event);
        }
    }

    @Override
    public FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now) {
        Flags classFlag = getConfiguration().getClassFlag() != null ? getConfiguration().getClassFlag() : Flags.CLASS;
        TimePoint gateShutdownTime = getGateShutdownTimePoint(startTime);
        if (now.before(startTime.minus(startPhaseClassOverGolfUpInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(classFlag, Flags.GOLF, false), new FlagPole(Flags.PAPA,
                    false)), null, Arrays.asList(new FlagPole(classFlag, Flags.GOLF, true), new FlagPole(Flags.PAPA,
                    false)), startTime.minus(startPhaseClassOverGolfUpInterval));
        } else if (now.before(startTime.minus(startPhasePapaUpInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(classFlag, Flags.GOLF, true), new FlagPole(Flags.PAPA,
                    false)), startTime.minus(startPhaseClassOverGolfUpInterval), Arrays.asList(new FlagPole(classFlag,
                    Flags.GOLF, true), new FlagPole(Flags.PAPA, true)), startTime.minus(startPhasePapaUpInterval));
        } else if (now.before(startTime.minus(startPhasePapaDownInterval))) {
            return new FlagPoleState(Arrays.asList(new FlagPole(classFlag, Flags.GOLF, true), new FlagPole(Flags.PAPA,
                    true)), startTime.minus(startPhasePapaUpInterval), Arrays.asList(new FlagPole(classFlag,
                    Flags.GOLF, true), new FlagPole(Flags.PAPA, false)), startTime.minus(startPhasePapaDownInterval));
        } else if (now.before(startTime)) {
            return new FlagPoleState(Arrays.asList(new FlagPole(classFlag, Flags.GOLF, true), new FlagPole(Flags.PAPA,
                    false)), startTime.minus(startPhasePapaDownInterval), Arrays.asList(new FlagPole(classFlag, false),
                    new FlagPole(Flags.GOLF, true)), startTime);
        } else if (now.before(gateShutdownTime)) {
            return new FlagPoleState(Arrays.asList(new FlagPole(Flags.GOLF, true)), startTime,
                    Arrays.asList(new FlagPole(Flags.GOLF, false)), gateShutdownTime);
        } else {
            if (isFinished(now)) {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.BLUE, false)), getFinishedTime());
            } else if (isInFinishingPhase(now)) {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.BLUE, true)), getFinishingTime(),
                        Arrays.asList(new FlagPole(Flags.BLUE, false)), null);
            } else {
                return new FlagPoleState(Arrays.asList(new FlagPole(Flags.GOLF, false)), gateShutdownTime);
            }
        }
    }

    @Override
    public TimePoint getGateLaunchStopTimePoint(TimePoint startTime) {
        return startTime.plus(getGateLaunchStopTime());
    }

    @Override
    public TimePoint getGateShutdownTimePoint(TimePoint startTime) {
        return getGateLaunchStopTimePoint(startTime).plus(getGolfDownTime());
    }

    @Override
    public long getGateLaunchStopTime() {
        return cachedGateLineOpeningTimes.getGateLaunchStopTime();
    }

    @Override
    public long getGolfDownTime() {
        return cachedGateLineOpeningTimes.getGolfDownTime();
    }

    @Override
    public void setGateLineOpeningTimes(TimePoint now, long gateLaunchStopTime, long golfDownTime) {
        raceLog.add(new RaceLogGateLineOpeningTimeEventImpl(now, author, raceLog.getCurrentPassId(), gateLaunchStopTime,
                golfDownTime));
    }

    @Override
    public String getPathfinder() {
        return cachedPathfinder;
    }

    @Override
    public void setPathfinder(TimePoint timePoint, String sailingId) {
        raceLog.add(new RaceLogPathfinderEventImpl(timePoint, author, raceLog.getCurrentPassId(), sailingId));
    }

    @Override
    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer() {
        return new GateStartChangedListeners();
    }

    @Override
    protected GateStartChangedListeners getChangedListeners() {
        return (GateStartChangedListeners) super.getChangedListeners();
    }

    @Override
    public void addChangedListener(GateStartChangedListener listener) {
        getChangedListeners().add(listener);
    }

    @Override
    protected void update() {
        GateLineOpeningTimes gateLineOpeningTimes = gateLineOpeningTimeAnalyzer.analyze();
        if (gateLineOpeningTimes != null
                && (!gateLineOpeningTimes.equals(cachedGateLineOpeningTimes) || !gateLineOpeningTimesHasBeenSet)) {
            cachedGateLineOpeningTimes = gateLineOpeningTimes;
            gateLineOpeningTimesHasBeenSet = true;
            getChangedListeners().onGateLaunchTimeChanged(this);
        }

        String pathfinder = pathfinderAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedPathfinder, pathfinder)) {
            cachedPathfinder = pathfinder;
            getChangedListeners().onPathfinderChanged(this);
        }

        super.update();
    }

    @Override
    public GateStartConfiguration getConfiguration() {
        return (GateStartConfiguration) super.getConfiguration();
    }

    private void rescheduleGateShutdownTime(TimePoint startTime) {
        unscheduleStateEvent(RaceStateEvents.GATE_SHUTDOWN);
        if (startTime != null) {
            scheduleStateEvents(new RaceStateEventImpl(getGateShutdownTimePoint(startTime),
                    RaceStateEvents.GATE_SHUTDOWN));
        }
    }

}
