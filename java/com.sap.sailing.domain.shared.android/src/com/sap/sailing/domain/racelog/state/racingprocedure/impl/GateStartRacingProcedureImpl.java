package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.base.configuration.procedures.GateStartConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.GateLineOpeningTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.PathfinderFinder;
import com.sap.sailing.domain.racelog.state.RaceState;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;
import com.sap.sailing.domain.racelog.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;

public class GateStartRacingProcedureImpl extends BaseRacingProcedure implements GateStartRacingProcedure {
    
    private final static long startPhaseClassOverGolfUpIntervall = 8 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseGolfDownStandardIntervalConstantSummand = 3 * 60 * 1000; // minutes * seconds * milliseconds
    
    private final GateStartConfiguration configuration;
    private final GateLineOpeningTimeFinder gateLineOpeningTimeAnalyzer;
    private final PathfinderFinder pathfinderAnalyzer;;
    
    private Long cachedGateLineOpeningTime;
    private String cachedPathfinder;
    
    public GateStartRacingProcedureImpl(RaceLog raceLog, RaceLogEventAuthor author, 
            RaceLogEventFactory factory, GateStartConfiguration configuration) {
        super(raceLog, author, factory);
        this.configuration = configuration;
        this.gateLineOpeningTimeAnalyzer = new GateLineOpeningTimeFinder(raceLog);
        this.pathfinderAnalyzer = new PathfinderFinder(raceLog);
        
        this.cachedGateLineOpeningTime = GateStartRacingProcedure.DefaultGolfDownTimeout;
        
        update();
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.GateStart;
    }
    
    @Override
    public boolean hasIndividualRecall() {
        return false;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime, TimePoint now) {
        return null;
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        if (now.before(startTime)) {
            long timeTillStart = startTime.minus(now.asMillis()).asMillis();
            return timeTillStart < startPhaseClassOverGolfUpIntervall;
        }
        return false;
    }
    
    @Override
    public void triggerStateEventScheduling(RaceState state) {
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
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(startPhaseClassOverGolfUpIntervall), RaceStateEvents.GATE_CLASS_OVER_GOLF_UP),
                new RaceStateEventImpl(startTime.minus(startPhasePapaUpInterval), RaceStateEvents.GATE_PAPA_UP),
                new RaceStateEventImpl(startTime.minus(startPhasePapaDownInterval), RaceStateEvents.GATE_PAPA_DOWN),
                new RaceStateEventImpl(startTime, RaceStateEvents.START));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case START:
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
        TimePoint gateShutdownTime = getGateShutdownTime(startTime);
        if (now.before(startTime.minus(startPhaseClassOverGolfUpIntervall))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, false), new FlagPole(Flags.PAPA, false)), 
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, false)),  
                    startTime.minus(startPhaseClassOverGolfUpIntervall));
        } else if (now.before(startTime.minus(startPhasePapaUpInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, false)), 
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, true)),
                    startTime.minus(startPhasePapaUpInterval));
        } else if (now.before(startTime.minus(startPhasePapaDownInterval))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, true)),
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, false)),
                    startTime.minus(startPhasePapaDownInterval));
        } else if (now.before(startTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.CLASS, Flags.GOLF, true), new FlagPole(Flags.PAPA, false)), 
                    Arrays.asList(new FlagPole(Flags.CLASS, false), new FlagPole(Flags.GOLF, true)),
                    startTime);
        } else if (now.before(gateShutdownTime)) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.GOLF, true)),
                    Arrays.asList(new FlagPole(Flags.GOLF, false)),
                    gateShutdownTime);
        } else {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(Flags.GOLF, false)));
        }
    }

    @Override
    public TimePoint getGateLaunchStopTime(TimePoint startTime) {
        return startTime.plus(getGateLaunchTime());
    }
    
    @Override
    public TimePoint getGateShutdownTime(TimePoint startTime) {
        return getGateLaunchStopTime(startTime).plus(startPhaseGolfDownStandardIntervalConstantSummand);
    }

    @Override
    public Long getGateLaunchTime() {
        return cachedGateLineOpeningTime;
    }

    @Override
    public void setGateLaunchTime(TimePoint timePoint, long milliseconds) {
        raceLog.add(factory.createGateLineOpeningTimeEvent(timePoint, author, raceLog.getCurrentPassId(), milliseconds));
    }

    @Override
    public String getPathfinder() {
        return cachedPathfinder;
    }

    @Override
    public void setPathfinder(TimePoint timePoint, String sailingId) {
        raceLog.add(factory.createPathfinderEvent(timePoint, author, raceLog.getCurrentPassId(), sailingId));
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
        Long gateLineOpeningTime = gateLineOpeningTimeAnalyzer.analyze();
        if (gateLineOpeningTime != null && !gateLineOpeningTime.equals(cachedGateLineOpeningTime)) {
            cachedGateLineOpeningTime = gateLineOpeningTime;
            getChangedListeners().onGateLaunchTimeChanged(this);
        }
        
        String pathfinder = pathfinderAnalyzer.analyze();
        if (!Util.equalsWithNull(cachedPathfinder, pathfinder)) {
            cachedPathfinder = pathfinder;
            getChangedListeners().onPathfinderChanged(this);
        }
        
        super.update();
    }

    private void rescheduleGateShutdownTime(TimePoint startTime) {
        unscheduleStateEvent(RaceStateEvents.GATE_SHUTDOWN);
        if (startTime != null) {
            scheduleStateEvents(new RaceStateEventImpl(getGateShutdownTime(startTime), RaceStateEvents.GATE_SHUTDOWN));
        }
    }

}
