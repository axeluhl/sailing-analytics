package com.sap.sailing.racecommittee.app.domain.startprocedure.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureRaceStateChangedListener;

public class ExtremeSailingSeriesStartProcedure implements StartProcedure {
    
    private final static long startPhaseAPDownInterval = 4 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSThreeUpInterval = 3 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSTwoUpInterval = 2 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneUpInterval = 1 * 60 * 1000; // minutes * seconds * milliseconds
    private final static long startPhaseESSOneDownInterval = 0;
    
    private List<Long> startProcedureEventIntervals;
    private PassAwareRaceLog raceLog;
    private StartProcedureRaceStateChangedListener raceStateChangedListener;
    
    public ExtremeSailingSeriesStartProcedure(PassAwareRaceLog raceLog) {
        this.raceLog = raceLog;
        startProcedureEventIntervals = new ArrayList<Long>();
        raceStateChangedListener = null;
        
        startProcedureEventIntervals.add(startPhaseAPDownInterval);
        startProcedureEventIntervals.add(startPhaseESSThreeUpInterval);
        startProcedureEventIntervals.add(startPhaseESSTwoUpInterval);
        startProcedureEventIntervals.add(startPhaseESSOneUpInterval);
        startProcedureEventIntervals.add(startPhaseESSOneDownInterval);
    }

    @Override
    public TimePoint getStartTimeEventTime() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TimePoint> getTriggerEventTimePoints(TimePoint startTime) {
        List<TimePoint> triggerTimePoints = new ArrayList<TimePoint>();
        
        for (Long interval : startProcedureEventIntervals) {
            triggerTimePoints.add(startTime.minus(interval));
        }
        return triggerTimePoints;
    }

    @Override
    public void dispatchTriggeredEventTimePoint(TimePoint startTime, TimePoint eventTime) {
        long interval = startTime.asMillis() - eventTime.asMillis();
        
        if (interval == startPhaseAPDownInterval) {
            handleAPDown(eventTime);
        } else if (interval == startPhaseESSThreeUpInterval) {
            handleEssThreeUp(eventTime);
        } else if (interval == startPhaseESSTwoUpInterval) {
            handleEssTwoUpAndEssThreeDown(eventTime);
        } else if (interval == startPhaseESSOneUpInterval) {
            handleEssOneUpAndEssTwoDown(eventTime);
        } else if (interval == startPhaseESSOneDownInterval) {
            handleEssOneDown(eventTime);
        }
    }

    private void handleAPDown(TimePoint eventTime) {
        TimePoint startPhaseTimePoint = eventTime.minus(1);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStartphaseEntered(startPhaseTimePoint);
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.AP, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(event);
    }

    private void handleEssThreeUp(TimePoint eventTime) {
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTHREE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(event);
    }

    private void handleEssTwoUpAndEssThreeDown(TimePoint eventTime) {
        TimePoint essThreeDownTimePoint = eventTime.minus(1);
        
        RaceLogEvent essThreeDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(essThreeDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTHREE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essThreeDownEvent);
        
        RaceLogEvent essTwoUpEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTWO, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(essTwoUpEvent);
    }

    private void handleEssOneUpAndEssTwoDown(TimePoint eventTime) {
        TimePoint essTwoDownTimePoint = eventTime.minus(1);
        
        RaceLogEvent essTwoDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(essTwoDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSTWO, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essTwoDownEvent);
        
        RaceLogEvent essOneUpEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(eventTime, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSONE, Flags.NONE, /*isDisplayed*/true);
        raceLog.add(essOneUpEvent);
    }

    private void handleEssOneDown(TimePoint eventTime) {
        TimePoint essOneDownTimePoint = eventTime.minus(1);
        RaceLogEvent essOneDownEvent = RaceLogEventFactory.INSTANCE.createFlagEvent(essOneDownTimePoint, UUID.randomUUID(), Collections.<Competitor>emptyList(), 
                raceLog.getCurrentPassId(), Flags.ESSONE, Flags.NONE, /*isDisplayed*/false);
        raceLog.add(essOneDownEvent);
        
        if (raceStateChangedListener != null) {
            raceStateChangedListener.onRaceStarted(eventTime);
        }
    }
    
    @Override
    public void setRaceStateChangedListener(StartProcedureRaceStateChangedListener raceStateChangedListener) {
        this.raceStateChangedListener = raceStateChangedListener;
    }

}
