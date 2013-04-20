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
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartPhaseEventListener;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureRaceStateChangedListener;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase.GateStartPhaseFragment;

public class GateStartProcedure implements StartProcedure {
    
    private final static long startPhaseClassOverGolfUpIntervall = 8 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaUpInterval = 5 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhasePapaDownInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseClassOverGolfDownInterval = 0 * 60 * 1000; // minutes * seconds * milliseconds
    
    private final static long individualRecallRemovalInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    
    private List<Long> startProcedureEventIntervals;
    private PassAwareRaceLog raceLog;
    private StartProcedureRaceStateChangedListener raceStateChangedListener;
    private GateStartPhaseEventListener startPhaseEventListener;
    
    public GateStartProcedure(PassAwareRaceLog raceLog) {
        this.raceLog = raceLog;
        startProcedureEventIntervals = new ArrayList<Long>();
        raceStateChangedListener = null;
        startPhaseEventListener = null;
        
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
        TimePoint startPhaseTimePoint = eventTime.minus(1);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStartphaseEntered(startPhaseTimePoint);
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
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
        TimePoint essThreeDownTimePoint = eventTime.minus(1);
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(essThreeDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.PAPA, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        RaceLogEvent essTwoUpEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTWO, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(essTwoUpEvent);
        
        if (startPhaseEventListener != null) {
            startPhaseEventListener.onPapaDown();
        }
    }

    private void handleClassOverGolfDown(TimePoint eventTime) {
        TimePoint essOneDownTimePoint = eventTime.minus(1);
        RaceLogEvent essOneDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(essOneDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.CLASS, Flags.GOLF, /*isDisplayed*/false);
        raceLog.add(essOneDownEvent);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStarted(eventTime);
        }
    }
    
    @Override
    public void setRaceStateChangedListener(StartProcedureRaceStateChangedListener raceStateChangedListener) {
        this.raceStateChangedListener = raceStateChangedListener;
    }

    @Override
    public TimePoint getLogicalStartTimeEventTime(TimePoint newEnteredStartTime) {
        TimePoint now = MillisecondsTimePoint.now();
        TimePoint startPhaseStartTime = getStartPhaseStartTime(newEnteredStartTime);
        TimePoint resultTime;
        if (now.after(startPhaseStartTime)) {
            resultTime = startPhaseStartTime.minus(1);
        } else {
            resultTime = now;
        }
        
        return resultTime;
    }

    @Override
    public void setFinishing(TimePoint eventTime) {
        TimePoint blueFlagDisplayEventTime = eventTime.minus(1);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(blueFlagDisplayEventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.BLUE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceFinishing(eventTime);
        }
    }
    
    public void dispatchAutomaticRaceEndEvent(TimePoint automaticRaceEnd) {

    }

    @Override
    public void setFinished(TimePoint eventTime) {
        TimePoint blueFlagRemoveEventTime = eventTime.minus(1);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(blueFlagRemoveEventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
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
        TimePoint apFlagDisplayEventTime = eventTime.minus(1);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(apFlagDisplayEventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
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
        TimePoint novemberFlagDisplayEventTime = eventTime.minus(1);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(novemberFlagDisplayEventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.NOVEMBER, lowerFlag, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setGeneralRecall(TimePoint eventTime) {
        TimePoint recallDisplayEventTime = eventTime.minus(1);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(recallDisplayEventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.FIRSTSUBSTITUTE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceAborted(eventTime);
        }
    }

    @Override
    public void setIndividualRecall(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
        
        TimePoint individualRecallRemovalFireTimePoint = eventTime.plus(individualRecallRemovalInterval);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onIndividualRecall(individualRecallRemovalFireTimePoint);
        }
    }

    @Override
    public void dispatchFiredIndividualRecallRemovalEvent(TimePoint individualRecallDisplayedTime, TimePoint eventTime) {
        if (individualRecallDisplayedTime != null) {
            long interval = eventTime.asMillis() - individualRecallDisplayedTime.asMillis();
            
            if (interval == individualRecallRemovalInterval) {
                setIndividualRecallRemoval(eventTime);
            }
        }
    }

    @Override
    public void setIndividualRecallRemoval(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onIndividualRecallRemoval();
        }
    }

    @Override
    public Class<? extends RaceFragment> getStartphaseFragment() {
        return GateStartPhaseFragment.class;
    }

    @Override
    public void setStartPhaseEventListener(StartPhaseEventListener listener) {
        startPhaseEventListener = (GateStartPhaseEventListener) listener;
    }

    @Override
    public Pair<String, Long> getNextFlagCountdownUiLabel(Context context, long millisecondsTillStart) {
        Pair<String, Long> result;
        if (millisecondsTillStart < startPhasePapaDownInterval) {
            result = new Pair<String, Long>(context.getResources().getString(R.string.race_startphase_gate_class_over_golf_removed), millisecondsTillStart);
        } else if (millisecondsTillStart < startPhasePapaUpInterval) {
            result = new Pair<String, Long>(context.getResources().getString(R.string.race_startphase_gate_papa_removed), millisecondsTillStart - startPhasePapaDownInterval);
        } else if (millisecondsTillStart < startPhaseClassOverGolfUpIntervall) {
            result = new Pair<String, Long>(context.getResources().getString(R.string.race_startphase_gate_papa_display), millisecondsTillStart - startPhasePapaUpInterval);
        } else {
            result = new Pair<String, Long>(context.getResources().getString(R.string.race_startphase_gate_class_over_golf_display), millisecondsTillStart - startPhaseClassOverGolfUpIntervall);
        }
        return result;
    }

}
