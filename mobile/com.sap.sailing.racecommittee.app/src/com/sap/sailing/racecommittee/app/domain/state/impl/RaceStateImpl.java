package com.sap.sailing.racecommittee.app.domain.state.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.racecommittee.app.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.state.impl.analyzers.FinishedTimeFinder;
import com.sap.sailing.racecommittee.domain.state.impl.analyzers.RaceStatusAnalyzer;
import com.sap.sailing.racecommittee.domain.state.impl.analyzers.StartTimeFinder;

public class RaceStateImpl implements RaceState, RaceLogChangedListener {
    // private static final String TAG = RaceStateImpl.class.getName();

    protected PassAwareRaceLog raceLog;
    protected StartProcedure startProcedure;
    
    protected RaceLogRaceStatus status;
    protected Set<RaceStateChangedListener> stateChangedListeners;

    private RaceLogChangedVisitor raceLogListener;
    private RaceStatusAnalyzer statusAnalyzer;
    private StartTimeFinder startTimeFinder;
    private FinishedTimeFinder finishedTimeFinder;

    public RaceStateImpl(PassAwareRaceLog raceLog, StartProcedure procedure) {
        this.raceLog = raceLog;
        this.startProcedure = procedure;
        
        this.status = RaceLogRaceStatus.UNKNOWN;
        this.stateChangedListeners = new HashSet<RaceStateChangedListener>();

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.startTimeFinder = new StartTimeFinder(raceLog);
        this.finishedTimeFinder = new FinishedTimeFinder(raceLog);
        this.statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        updateStatus();
    }

    public RaceLog getRaceLog() {
        return raceLog;
    }

    public RaceLogRaceStatus getStatus() {
        return status;
    }

    private void setStatus(RaceLogRaceStatus newStatus) {
        RaceLogRaceStatus oldStatus = this.status;
        this.status = newStatus;
        if (oldStatus != newStatus) {
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (RaceStateChangedListener listener : stateChangedListeners) {
            listener.onRaceStateChanged(this);
        }
    }

    public void registerListener(RaceStateChangedListener listener) {
        stateChangedListeners.add(listener);
    }

    public void unregisterListener(RaceStateChangedListener listener) {
        stateChangedListeners.remove(listener);
    }

    public TimePoint getStartTime() {
        return startTimeFinder.getStartTime();
    }

    public void setStartTime(TimePoint newStartTime) {
        TimePoint eventTime = startProcedure.getStartTimeEventTime();
        
        RaceLogRaceStatus status = getStatus();
        if (status != RaceLogRaceStatus.UNSCHEDULED) {
            onRaceAborted(eventTime.minus(1));
        }

        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createStartTimeEvent(eventTime, UUID.randomUUID(), 
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), newStartTime);
        this.raceLog.add(event);
    }

    public TimePoint getFinishedTime() {
        return finishedTimeFinder.getFinishedTime();
    }
    
    public void setCourseDesign(CourseData courseData) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), courseData);
        this.raceLog.add(event);
    }

    public void onRaceAborted(TimePoint eventTime) {
        RaceLogEvent abortEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.UNSCHEDULED);
        this.raceLog.add(abortEvent);

        RaceLogEvent passChangeEvent = RaceLogEventFactory.INSTANCE.createRaceLogPassChangeEvent(eventTime, raceLog.getCurrentPassId() + 1);
        this.raceLog.add(passChangeEvent);
    }

    public void onRaceStarted(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.RUNNING);
        this.raceLog.add(statusEvent);
    }

    public void onRaceFinishing(TimePoint eventTime) {
        RaceLogEvent statusEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.FINISHING);
        this.raceLog.add(statusEvent);
    }

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

}
