package com.sap.sailing.racecommittee.app.domain.state.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.impl.RaceLogPassChangeEventImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.racelog.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.racecommittee.app.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.racecommittee.app.domain.state.RaceLogChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.RaceState;
import com.sap.sailing.racecommittee.app.domain.state.RaceStateChangedListener;
import com.sap.sailing.racecommittee.app.domain.state.impl.analyzers.RaceStatusAnalyzer;
import com.sap.sailing.racecommittee.app.domain.state.impl.analyzers.StartTimeFinder;

public class RaceStateImpl implements RaceState, RaceLogChangedListener {
    // private static final String TAG = RaceStateImpl.class.getName();

    protected RaceLogRaceStatus status;
    protected PassAwareRaceLog raceLog;
    protected Set<RaceStateChangedListener> changedListeners;

    private RaceLogChangedVisitor raceLogListener;
    private RaceStatusAnalyzer statusAnalyzer;
    private StartTimeFinder startTimeFinder;

    public RaceStateImpl(PassAwareRaceLog raceLog) {
        this.raceLog = raceLog;
        this.status = RaceLogRaceStatus.UNKNOWN;
        this.changedListeners = new HashSet<RaceStateChangedListener>();

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.startTimeFinder = new StartTimeFinder(raceLog);
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
        for (RaceStateChangedListener listener : changedListeners) {
            listener.onRaceStateChanged(this);
        }
    }

    public void registerListener(RaceStateChangedListener listener) {
        changedListeners.add(listener);
    }

    public void unregisterListener(RaceStateChangedListener listener) {
        changedListeners.remove(listener);
    }

    public TimePoint getStartTime() {
        return startTimeFinder.getStartTime();
    }

    public void setStartTime(TimePoint eventTime, TimePoint newStartTime) {
        RaceLogRaceStatus status = getStatus();
        if (status != RaceLogRaceStatus.UNSCHEDULED) {
            abortRace(eventTime.minus(1));
        }

        RaceLogEvent event = new RaceLogStartTimeEventImpl(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), RaceLogRaceStatus.SCHEDULED,
                newStartTime);
        this.raceLog.add(event);
    }

    public void abortRace(TimePoint eventTime) {
        RaceLogEvent abortEvent = new RaceLogRaceStatusEventImpl(eventTime.minus(1), UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId(), RaceLogRaceStatus.UNSCHEDULED);
        this.raceLog.add(abortEvent);

        RaceLogEvent passChangeEvent = new RaceLogPassChangeEventImpl(eventTime, UUID.randomUUID(),
                Collections.<Competitor> emptyList(), raceLog.getCurrentPassId() + 1);
        this.raceLog.add(passChangeEvent);
    }

    public RaceLogRaceStatus updateStatus() {
        setStatus(statusAnalyzer.getStatus());
        return getStatus();
    }

    public void eventAdded(RaceLogEvent event) {
        updateStatus();
    }

}
