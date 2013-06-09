package com.sap.sailing.racecommittee.app.domain.state.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.StartProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.ProtestStartTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartProcedureTypeAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.racecommittee.app.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.impl.StartProcedureFactory;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateEventListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RaceStateImpl implements RaceState, RaceLogChangedListener {
    private static final String TAG = RaceStateImpl.class.getSimpleName();
    
    protected StartProcedureType defaultStartProcedureType;
    protected RaceLog raceLog;
    protected StartProcedure startProcedure;
    
    protected RaceLogRaceStatus status;
    protected Set<RaceStateChangedListener> stateChangedListeners;
    protected Set<RaceStateEventListener> stateEventListeners;

    private RaceLogChangedVisitor raceLogListener;
    
    private RaceStatusAnalyzer statusAnalyzer;
    private StartTimeFinder startTimeFinder;
    private FinishedTimeFinder finishedTimeFinder;
    private FinishingTimeFinder finishingTimeFinder;
    private LastPublishedCourseDesignFinder lastCourseDesignFinder;
    private FinishPositioningListFinder finishPositioningListFinder;
    private StartProcedureTypeAnalyzer startProcedureTypeAnalyzer;
    private ProtestStartTimeFinder protestStartTimeAnalyzer;

    public RaceStateImpl(StartProcedureType defaultStartProcedureType, RaceLog raceLog) {
        this.defaultStartProcedureType = defaultStartProcedureType;
        this.raceLog = raceLog;
        
        this.status = RaceLogRaceStatus.UNKNOWN;
        this.stateChangedListeners = new HashSet<RaceStateChangedListener>();
        this.stateEventListeners = new HashSet<RaceStateEventListener>();

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.startTimeFinder = new StartTimeFinder(raceLog);
        this.finishingTimeFinder = new FinishingTimeFinder(raceLog);
        this.finishedTimeFinder = new FinishedTimeFinder(raceLog);
        this.statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        this.lastCourseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
        this.finishPositioningListFinder = new FinishPositioningListFinder(raceLog);
        this.startProcedureTypeAnalyzer = new StartProcedureTypeAnalyzer(raceLog);
        this.protestStartTimeAnalyzer = new ProtestStartTimeFinder(raceLog);
        
        registerStartProcedure();
        updateStatus();
    }
    
    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }
    
    @Override
    public RaceLogRaceStatus getStatus() {
        return status;
    }
    
    @Override
    public StartProcedure getStartProcedure() {
        return startProcedure;
    }

    @Override
    public StartProcedureType getStartProcedureType() {
        StartProcedureType type = startProcedureTypeAnalyzer.getActiveStartProcedureType();
        return type == null ? defaultStartProcedureType : type;
    }

    private void registerStartProcedure() {
        if (startProcedure != null) {
            startProcedure.setStartProcedureListener(null);
        }
        StartProcedureType type = getStartProcedureType();
        startProcedure = StartProcedureFactory.create(type, raceLog);
        startProcedure.setStartProcedureListener(this);
    }

    private void setStatus(RaceLogRaceStatus newStatus) {
        RaceLogRaceStatus oldStatus = this.status;
        this.status = newStatus;
        if (oldStatus != newStatus) {
            fireStatusChanged();
        }
    }

    public TimePoint getStartTime() {
        return startTimeFinder.getStartTime();
    }

    public void setStartTime(TimePoint newStartTime) {
        
        RaceLogRaceStatus status = getStatus();
        if (status != RaceLogRaceStatus.UNSCHEDULED) {
            onRaceAborted(MillisecondsTimePoint.now());
        }
        
        TimePoint eventTime = startProcedure.getLogicalStartTimeEventTime(newStartTime);
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(eventTime, UUID.randomUUID(), 
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), newStartTime);
        this.raceLog.add(event);
        
        fireStartTimeChange(newStartTime);
    }
    
    @Override
    public void createNewStartProcedure(StartProcedureType type) {
        if (!type.equals(startProcedureTypeAnalyzer.getActiveStartProcedureType())) {

            RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartProcedureChangedEvent(MillisecondsTimePoint.now(), raceLog.getCurrentPassId(), type);
            this.raceLog.add(event);
            
            registerStartProcedure();
            
            ExLog.i(TAG, String.format("Switch start procedure to %s", this.startProcedure.getClass().getSimpleName()));
        }
    }

    public TimePoint getFinishedTime() {
        return finishedTimeFinder.getFinishedTime();
    }
    
    @Override
    public CourseBase getCourseDesign() {
        return lastCourseDesignFinder.getLastCourseDesign();
    }
 
    public void setCourseDesign(CourseBase newCourseData) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), newCourseData);
        this.raceLog.add(event);
        
        fireCourseDesignChanged();
    }

    @Override
    public void onRaceAborted(TimePoint eventTime) {
        /*RaceLogEvent abortEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.UNSCHEDULED);
        this.raceLog.add(abortEvent);*/
        
        RaceLogEvent passChangeEvent = RaceLogEventFactory.INSTANCE.createPassChangeEvent(eventTime, raceLog.getCurrentPassId() + 1);
        this.raceLog.add(passChangeEvent);
        registerStartProcedure();
        fireRaceAborted();
    }
    
    @Override
    public void onRaceStartphaseEntered(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.STARTPHASE);
        this.raceLog.add(statusEvent);
    }

    @Override
    public void onRaceStarted(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.RUNNING);
        this.raceLog.add(statusEvent);
    }

    @Override
    public void onRaceFinishing(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING);
        this.raceLog.add(statusEvent);
    }
    
    @Override
    public void onRaceFinishing(TimePoint eventTime, TimePoint automaticRaceEnd) {
        onRaceFinishing(eventTime);
    }


    @Override
    public void onRaceFinished(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHED);
        this.raceLog.add(statusEvent);
    }

    public RaceLogRaceStatus updateStatus() {
        setStatus(statusAnalyzer.getStatus());
        return getStatus();
    }

    public void eventAdded(RaceLogEvent event) {
        updateStatus();
    }

    @Override
    public TimePoint getFinishingStartTime() {
        return finishingTimeFinder.getFinishingTime();
    }

    @Override
    public void setFinishPositioningListChanged(List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningListChangedEvent(eventTime, raceLog.getCurrentPassId(), positionedCompetitors);
        this.raceLog.add(event);        
    }
    
    @Override
    public List<Triple<Serializable, String, MaxPointsReason>> getFinishPositioningList() {
        return finishPositioningListFinder.getFinishPositioningList();
    }
    
    @Override
    public void setFinishPositioningConfirmed() {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(eventTime, raceLog.getCurrentPassId());
        this.raceLog.add(event);
    }

    @Override
    public void setProtestStartTime(TimePoint protestStartTime) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createProtestStartTimeEvent(eventTime, raceLog.getCurrentPassId(), protestStartTime);
        this.raceLog.add(event);
        fireProtestStartTimeChanged();
    }

    @Override
    public TimePoint getProtestStartTime() {
        return protestStartTimeAnalyzer.getProtestStartTime();
    }

    @Override
    public void onStartProcedureSpecificEvent(TimePoint eventTime, Integer eventId) {
        for (RaceStateEventListener listener : stateEventListeners) {
            listener.onStartProcedureSpecificEvent(eventTime, eventId);
        }
    }

    private void fireStatusChanged() {
        for (RaceStateChangedListener listener : stateChangedListeners) {
            listener.onRaceStateStatusChanged(this);
        }
    }

    private void fireCourseDesignChanged() {
        for (RaceStateChangedListener listener : stateChangedListeners) {
            listener.onRaceStateCourseDesignChanged(this);
        }
    }
    
    private void fireProtestStartTimeChanged() {
        for (RaceStateChangedListener listener : stateChangedListeners) {
            listener.onRaceStateProtestStartTimeChanged(this);
        }
    }
    
    private void fireStartTimeChange(TimePoint newStartTime) {
        for (RaceStateEventListener listener : stateEventListeners) {
            listener.onStartTimeChanged(newStartTime);
        }
    }
    
    private void fireRaceAborted() {
        for (RaceStateEventListener listener : stateEventListeners) {
            listener.onRaceAborted();
        }
    }

    public void registerStateChangeListener(RaceStateChangedListener listener) {
        stateChangedListeners.add(listener);
    }
    
    public void registerStateEventListener(RaceStateEventListener listener) {
        stateEventListeners.add(listener);
    }

    public void unregisterStateChangeListener(RaceStateChangedListener listener) {
        stateChangedListeners.remove(listener);
    }
    
    public void unregisterStateEventListener(RaceStateEventListener listener) {
        stateEventListeners.remove(listener);
    }
}
