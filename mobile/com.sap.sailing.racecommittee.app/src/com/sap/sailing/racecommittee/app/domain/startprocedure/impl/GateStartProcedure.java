package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import android.content.Context;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.RunningRaceEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.UserRequiredActionPerformedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishedRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.FinishingRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.GateStartRunningRaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.GateStartPhaseFragment;

public class GateStartProcedure implements StartProcedure {
    
    private final static long startPhaseClassOverGolfUpIntervall = 8 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaUpInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseClassOverGolfDownInterval = 0 * 60 * 1000; // minutes * seconds * milliseconds
    public final static long startPhaseGolfDownStandardInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    public final static long startPhaseGolfDownStandardIntervalConstantSummand = 3 * 60 * 1000; // minutes * seconds * milliseconds
    
    private List<Long> startProcedureEventIntervals;
    private RaceLog raceLog;
    private StartProcedureListener raceStateChangedListener;
    private GateStartPhaseEventListener startPhaseEventListener;
    private GateStartRunningRaceEventListener runningRaceEventListener;
    
    public GateStartProcedure(RaceLog raceLog) {
        this.raceLog = raceLog;
        startProcedureEventIntervals = new ArrayList<Long>();
        raceStateChangedListener = null;
        startPhaseEventListener = null;
        runningRaceEventListener = null;
        
        startProcedureEventIntervals.add(startPhaseClassOverGolfUpIntervall);
        startProcedureEventIntervals.add(startPhasePapaUpInterval);
        startProcedureEventIntervals.add(startPhasePapaDownInterval);
        startProcedureEventIntervals.add(startPhaseClassOverGolfDownInterval);
    }

    @Override
    public TimePoint getStartPhaseStartTime(TimePoint startTime) {
        return startTime.minus(startPhaseClassOverGolfUpIntervall);
    }

    @Override
    public List<TimePoint> getAutomaticEventFireTimePoints(TimePoint startTime) {
        List<TimePoint> triggerTimePoints = new ArrayList<TimePoint>();
        
        for (Long interval : startProcedureEventIntervals) {
            triggerTimePoints.add(startTime.minus(interval));
        }
        return triggerTimePoints;
    }

    @Override
    public void dispatchFiredEventTimePoint(TimePoint startTime, TimePoint eventTime) {
        long interval = startTime.asMillis() - eventTime.asMillis();
        
        if (interval == startPhaseClassOverGolfUpIntervall) {
            handleClassOverGolfUp(eventTime);
        } else if (interval == startPhasePapaUpInterval) {
            handlePapaUp(eventTime);
        } else if (interval == startPhasePapaDownInterval) {
            handlePapaDown(eventTime);
        } else if (interval == startPhaseClassOverGolfDownInterval) {
            handleClassOverGolfDown(eventTime);
        }
    }

    private void handleClassOverGolfUp(TimePoint eventTime) {
        TimePoint startPhaseTimePoint = eventTime;
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStartphaseEntered(startPhaseTimePoint);
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(startPhaseTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.CLASS, Flags.GOLF, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onClassOverGolfUp();
        }
    }

    private void handlePapaUp(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.PAPA, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onPapaUp();
        }
    }

    private void handlePapaDown(TimePoint eventTime) {
        TimePoint papaDownTimepoint = eventTime;
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(papaDownTimepoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.PAPA, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onPapaDown();
        }
    }

    private void handleClassOverGolfDown(TimePoint eventTime) {
        TimePoint essOneDownTimePoint = eventTime;
        
        RaceLogEvent essOneDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(essOneDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.CLASS, Flags.GOLF, /*isDisplayed*/false);
        raceLog.add(essOneDownEvent);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStarted(eventTime);
        }
    }
    
    @Override
    public void setStartProcedureListener(StartProcedureListener raceStateChangedListener) {
        this.raceStateChangedListener = raceStateChangedListener;
    }

    @Override
    public TimePoint getLogicalStartTimeEventTime(TimePoint newEnteredStartTime) {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startPhaseStartTime = getStartPhaseStartTime(newEnteredStartTime);
        TimePoint resultTime;
        if (now.after(startPhaseStartTime)) {
            // We do not want RaceStatusEvents with the same timestamp to ensure a reasonable ordering
            // Therefore we guarantee that SCHEDULDED happens before STARTPHASE
            resultTime = startPhaseStartTime.minus(1);
        } else {
            resultTime = now;
        }
        
        return resultTime;
    }

    @Override
    public void setFinishing(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinishing(eventTime);
        }
    }
    
    @Override
    public void dispatchAutomaticRaceEndEvent(TimePoint automaticRaceEnd) {

    }

    @Override
    public void setFinished(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinished(eventTime);
        }
    }

    @Override
    public void setPostponed(TimePoint eventTime, Flags lowerFlag) {
        switch (lowerFlag) {
        case NONE:
        case ALPHA:
        case HOTEL:
            handleAPUp(eventTime, lowerFlag);
            break;
        default:
            break;
        }
    }

    private void handleAPUp(TimePoint eventTime, Flags lowerFlag) {        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.AP, lowerFlag, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setAbandoned(TimePoint eventTime, Flags lowerFlag) {
        switch (lowerFlag) {
        case NONE:
        case ALPHA:
        case HOTEL:
            handleNovemberUp(eventTime, lowerFlag);
            break;
        default:
            break;
        }
    }

    private void handleNovemberUp(TimePoint eventTime, Flags lowerFlag) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.NOVEMBER, lowerFlag, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setGeneralRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public Class<? extends RaceFragment> getStartphaseFragment() {
        return GateStartPhaseFragment.class;
    }
    
    @Override
    public Class<? extends RaceFragment> getRunningRaceFragment() {
        return GateStartRunningRaceFragment.class;
    }

    @Override
    public void setStartPhaseEventListener(StartPhaseEventListener listener) {
        startPhaseEventListener = (GateStartPhaseEventListener) listener;
    }
    
    @Override
    public void setRunningRaceEventListener(RunningRaceEventListener listener) {
        runningRaceEventListener = (GateStartRunningRaceEventListener) listener;
    }

    @Override
    public Pair<String, List<Object>> getNextFlagCountdownUiLabel(Context context, long millisecondsTillStart) {
        Pair<String, List<Object>> result;
        List<Object> milisecondsList = new ArrayList<Object>();
        if (millisecondsTillStart < startPhasePapaDownInterval) {
            milisecondsList.add(millisecondsTillStart);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_gate_class_over_golf_removed), milisecondsList);
        } else if (millisecondsTillStart < startPhasePapaUpInterval) {
            milisecondsList.add(millisecondsTillStart - startPhasePapaDownInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_gate_papa_removed), milisecondsList);
        } else if (millisecondsTillStart < startPhaseClassOverGolfUpIntervall) {
            milisecondsList.add(millisecondsTillStart - startPhasePapaUpInterval);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_gate_papa_display), milisecondsList);
        } else {
            milisecondsList.add(millisecondsTillStart - startPhaseClassOverGolfUpIntervall);
            result = new Pair<String, List<Object>>(context.getResources().getString(R.string.race_startphase_gate_class_over_golf_display), milisecondsList);
        }
        return result;
    }

    public void dispatchAutomaticGateClose(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.GOLF, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (runningRaceEventListener != null) {
            runningRaceEventListener.onGolfDown();
        }
    }
    
    public void setGateLineOpeningTime(Long gateLineOpeningTimeInMinutes){
        //TODO
    }
    
    public void setPathfinder(String sailingId){
        //TODO
    }

    @Override
    public void handleStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId) {
        // TODO Auto-generated method stub
        
    }

    public String getPathfinder() {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getGateLineOpeningTime() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Class<? extends RaceFragment> getFinishingRaceFragment() {
        return FinishingRaceFragment.class;
    }

    @Override
    public Class<? extends RaceFragment> getFinishedRaceFragment() {
        return FinishedRaceFragment.class;
    }
    
    @Override
    public List<Class<? extends RaceDialogFragment>> checkForUserActionRequiredActions(MillisecondsTimePoint newStartTime, UserRequiredActionPerformedListener listener) {
        return new ArrayList<Class<? extends RaceDialogFragment>>();
    }
}
