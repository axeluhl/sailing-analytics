package com.sap.sailing.racecommittee.app.domain.state.impl;

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
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishPositioningListFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.LastPublishedCourseDesignFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceStatusAnalyzer;
import com.sap.sailing.domain.racelog.analyzing.impl.StartTimeFinder;
import com.sap.sailing.racecommittee.app.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.StartProcedure;

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
    private FinishingTimeFinder finishingTimeFinder;
    private LastPublishedCourseDesignFinder lastCourseDesignFinder;
    private FinishPositioningListFinder finishPositioningListFinder;

    public RaceStateImpl(PassAwareRaceLog raceLog, StartProcedure procedure) {
        this.raceLog = raceLog;
        this.startProcedure = procedure;
        
        this.status = RaceLogRaceStatus.UNKNOWN;
        this.stateChangedListeners = new HashSet<RaceStateChangedListener>();

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.startTimeFinder = new StartTimeFinder(raceLog);
        this.finishingTimeFinder = new FinishingTimeFinder(raceLog);
        this.finishedTimeFinder = new FinishedTimeFinder(raceLog);
        this.statusAnalyzer = new RaceStatusAnalyzer(raceLog);
        this.lastCourseDesignFinder = new LastPublishedCourseDesignFinder(raceLog);
        this.finishPositioningListFinder = new FinishPositioningListFinder(raceLog);
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
    
    @Override
    public CourseBase getCourseDesign() {
        return lastCourseDesignFinder.getLastCourseDesign();
    }

    
    public void setCourseDesign(CourseBase newCourseData) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createCourseDesignChangedEvent(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), newCourseData);
        this.raceLog.add(event);
    }

    public void onRaceAborted(TimePoint eventTime) {
        RaceLogEvent abortEvent = RaceLogEventFactory.INSTANCE.createRaceStatusEvent(eventTime, raceLog.getCurrentPassId(), RaceLogRaceStatus.UNSCHEDULED);
        this.raceLog.add(abortEvent);

        RaceLogEvent passChangeEvent = RaceLogEventFactory.INSTANCE.createPassChangeEvent(eventTime, raceLog.getCurrentPassId() + 1);
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

    @Override
    public TimePoint getFinishingStartTime() {
        return finishingTimeFinder.getFinishingTime();
    }

    @Override
    public void setFinishPositioningListChanged(List<Pair<Competitor,MaxPointsReason>> positionedCompetitors) {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningListChangedEvent(eventTime, raceLog.getCurrentPassId(), positionedCompetitors);
        this.raceLog.add(event);        
    }
    
    @Override
    public List<Pair<Competitor, MaxPointsReason>> getFinishPositioningList() {
        return finishPositioningListFinder.getFinishPositioningList();
    }
    
    @Override
    public void setFinishPositioningConfirmed() {
        TimePoint eventTime = MillisecondsTimePoint.now();
        
        RaceLogEvent event = RaceLogEventFactory.INSTANCE.createFinishPositioningConfirmedEvent(eventTime, raceLog.getCurrentPassId());
        this.raceLog.add(event);        
    }

}
