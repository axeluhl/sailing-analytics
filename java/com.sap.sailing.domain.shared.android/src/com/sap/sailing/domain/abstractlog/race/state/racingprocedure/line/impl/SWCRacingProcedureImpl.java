package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RacingProcedureTypeAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.SWCStartModeFlagFinder;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogFlagEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.FulfillmentFunction;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.NoMorePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line.SWCRacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

public class SWCRacingProcedureImpl extends ConfigurableStartModeFlagRacingProcedureImpl implements SWCRacingProcedure {

    private final static long CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL = 6 * 60 * 1000; // 6 minutes before start
    private final static long CLASS_OVER_OSCAR_AND_STARTMODE_DOWN_INTERVAL = 1 * 60 * 1000; // 1 minute after start

    private final SWCStartModeFlagFinder startModeFlagAnalyzer;
    
    private Flags cachedStartmodeFlag;
    private boolean startmodeFlagHasBeenSet;

    public SWCRacingProcedureImpl(RaceLog raceLog, AbstractLogEventAuthor author, 
             SWCStartConfiguration configuration, RaceLogResolver raceLogResolver) {
        super(raceLog, author, configuration, raceLogResolver);
        
        RacingProcedureTypeAnalyzer procedureAnalyzer = new RacingProcedureTypeAnalyzer(raceLog);
        if (configuration.getStartModeFlags() != null) {
            this.startModeFlagAnalyzer = new SWCStartModeFlagFinder(procedureAnalyzer, raceLog, configuration.getStartModeFlags());
        } else {
            this.startModeFlagAnalyzer = new SWCStartModeFlagFinder(procedureAnalyzer, raceLog);
        }
        
        this.cachedStartmodeFlag = SWCRacingProcedure.DefaultStartMode;
        this.startmodeFlagHasBeenSet = false;
        
        update();
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
    protected Boolean isResultEntryEnabledByDefault() {
        return false;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            FulfillmentFunction function) {
        return new NoMorePrerequisite(function);
    }

    @Override
    public boolean isStartphaseActive(TimePoint startTime, TimePoint now) {
        return now.before(startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL));
    }

    @Override
    protected Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime) {
        return Arrays.<RaceStateEvent> asList(
                new RaceStateEventImpl(startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL), RaceStateEvents.SWC_CLASS_OVER_OSCAR_UP),
                new RaceStateEventImpl(startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL), RaceStateEvents.SWC_STARTMODE_UP),
                new RaceStateEventImpl(startTime, RaceStateEvents.START),
                new RaceStateEventImpl(startTime.plus(CLASS_OVER_OSCAR_AND_STARTMODE_DOWN_INTERVAL), RaceStateEvents.SWC_STARTMODE_DOWN), 
                new RaceStateEventImpl(startTime.plus(CLASS_OVER_OSCAR_AND_STARTMODE_DOWN_INTERVAL), RaceStateEvents.SWC_CLASS_OVER_OSCAR_DOWN));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case SWC_STARTMODE_UP:
            if (!startmodeFlagHasBeenSet) {
                setStartModeFlag(event.getTimePoint(), cachedStartmodeFlag);
            }
        case SWC_CLASS_OVER_OSCAR_UP:
        case SWC_STARTMODE_DOWN:
        case SWC_CLASS_OVER_OSCAR_DOWN:
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
            
        if (now.before(startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag, Flags.OSCAR, false), new FlagPole(cachedStartmodeFlag, false)),
                    null,
                    Arrays.asList(new FlagPole(classFlag, Flags.OSCAR, true), new FlagPole(cachedStartmodeFlag, true)), 
                    startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL));
        } else if (now.before(startTime.plus(CLASS_OVER_OSCAR_AND_STARTMODE_DOWN_INTERVAL))) {
            return new FlagPoleState(
                    Arrays.asList(new FlagPole(classFlag,  Flags.OSCAR, true), new FlagPole(cachedStartmodeFlag, true)),
                    startTime.minus(CLASS_OVER_OSCAR_AND_STARTMODE_UP_INTERVAL),
                    Arrays.asList(new FlagPole(classFlag,  Flags.OSCAR, false), new FlagPole(cachedStartmodeFlag, false)), 
                    startTime.plus(CLASS_OVER_OSCAR_AND_STARTMODE_DOWN_INTERVAL));
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
    public void setStartModeFlag(TimePoint timePoint, Flags startMode) {
        raceLog.add(new RaceLogFlagEventImpl(timePoint, author, raceLog.getCurrentPassId(), startMode, Flags.NONE, true));
    }

    @Override
    public Flags getStartModeFlag() {
        return cachedStartmodeFlag;
    }

    @Override
    public Flags getDefaultStartMode() {
        return SWCRacingProcedure.DefaultStartMode;
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
            getChangedListeners().onStartModeChanged(this);
        }
        super.update();
    }
    
    @Override
    public SWCStartConfiguration getConfiguration() {
        return (SWCStartConfiguration) super.getConfiguration();
    }

}
