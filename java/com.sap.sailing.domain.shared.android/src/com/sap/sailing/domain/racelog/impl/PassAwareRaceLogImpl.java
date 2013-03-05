package com.sap.sailing.domain.racelog.impl;

import java.util.NavigableSet;

import com.sap.sailing.domain.racelog.PassAwareRaceLog;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.tracking.impl.PartialNavigableSetView;

public class PassAwareRaceLogImpl extends RaceLogImpl implements PassAwareRaceLog {
    // private static final String TAG = PassAwareRaceLogImpl.class.getName();
    private static final long serialVersionUID = -1252381252528365834L;
    private static final String ReadWriteLockName = PassAwareRaceLogImpl.class.getName() + ".lock";

    public static final int DefaultPassId = 0;

    private int currentPassId;

    public PassAwareRaceLogImpl(RaceLog raceLog) {
        this();
        try {
            raceLog.lockForRead();
            for (RaceLogEvent event : raceLog.getRawFixes()) {
                this.add(event);
            }
        } finally {
            raceLog.unlockAfterRead();
        }

    }

    public PassAwareRaceLogImpl() {
        this(DefaultPassId);
    }

    public PassAwareRaceLogImpl(int currentPassId) {
        super(ReadWriteLockName);
        this.currentPassId = currentPassId;
    }

    public int getCurrentPassId() {
        return currentPassId;
    }

    public void setCurrentPassId(int newPassId) {
        if (newPassId != this.currentPassId) {
            // ExLog.i(TAG, String.format("Changing pass id to %d", newPassId));
            this.currentPassId = newPassId;
        }
    }

    @Override
    public NavigableSet<RaceLogEvent> getFixes() {
        lockForRead();
        try {
            return super.getFixes();
        } finally {
            unlockAfterRead();
        }
    }

    @Override
    protected NavigableSet<RaceLogEvent> getInternalFixes() {
        return new PartialNavigableSetView<RaceLogEvent>(super.getInternalFixes()) {
            @Override
            protected boolean isValid(RaceLogEvent e) {
                return e.getPassId() == getCurrentPassId();
            }
        };
    }

    @Override
    public boolean add(RaceLogEvent event) {
        boolean isAdded = false;
        lockForWrite();
        try {
            isAdded = getInternalRawFixes().add(event);
        } finally {
            unlockAfterWrite();
        }
        if (isAdded) {
            // ExLog.i(TAG, String.format("%s (%s) was added to log.", event, event.getClass().getName()));
            setCurrentPassId(Math.max(event.getPassId(), this.currentPassId));
            notifyListenersAboutReceive(event);
        } else {
            // ExLog.w(TAG, String.format("%s (%s) was not added to log. Ignoring", event, event.getClass().getName()));
        }
        return isAdded;
    }
}
