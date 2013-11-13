package com.sap.sailing.domain.racelog.state.racingprocedure.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogChangedListener;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.RaceLogEventFactory;
import com.sap.sailing.domain.racelog.RaceLogEventVisitor;
import com.sap.sailing.domain.racelog.analyzing.impl.IndividualRecallDisplayedFinder;
import com.sap.sailing.domain.racelog.analyzing.impl.IsIndividualRecallDisplayedAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogChangedVisitor;
import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.RaceStateEvent;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;
import com.sap.sailing.domain.racelog.state.impl.BaseRaceState2ChangedListener;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.racelog.state.impl.RaceStateEvents;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;

/**
 * Base class for all your {@link RacingProcedure2}s.
 * 
 * It is a good idea to call {@link BaseRacingProcedure#update()} in your constructor to ensure your procedure is
 * correctly initialized.
 * 
 */
public abstract class BaseRacingProcedure extends BaseRaceState2ChangedListener implements RacingProcedure2,
        RaceLogChangedListener {

    private final static long individualRecallRemovalTimeout = 4 * 60 * 1000; // minutes * seconds * milliseconds

    protected RaceStateEventScheduler scheduler;

    protected final RaceLog raceLog;
    protected final RaceLogEventAuthor author;
    protected final RaceLogEventFactory factory;

    private final RacingProcedureChangedListeners<?> changedListeners;
    private final IsIndividualRecallDisplayedAnalyzer isRecallDisplayedAnalyzer;
    private final RaceLogEventVisitor raceLogListener;

    private boolean cachedIsIndividualRecallDisplayed;

    public BaseRacingProcedure(RaceLog raceLog, RaceLogEventAuthor author, RaceLogEventFactory factory) {
        this.raceLog = raceLog;
        this.author = author;
        this.factory = factory;

        this.changedListeners = createChangedListenerContainer();
        this.isRecallDisplayedAnalyzer = new IsIndividualRecallDisplayedAnalyzer(raceLog);

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.cachedIsIndividualRecallDisplayed = false;
    }
    
    @Override
    public void detachFromRaceLog() {
        raceLog.removeListener(raceLogListener);
    }

    protected RacingProcedureChangedListeners<?> getChangedListeners() {
        return changedListeners;
    }

    protected abstract RacingProcedureChangedListeners<?> createChangedListenerContainer();

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    @Override
    public void setStateEventScheduler(RaceStateEventScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void triggerStateEventScheduling(RaceState2 state) {
        switch (state.getStatus()) {
        case SCHEDULED:
        case STARTPHASE:
            onStartTimeChanged(state);
            break;
        case RUNNING:
            if (isIndividualRecallDisplayed()) {
                scheduleIndividualRecallTimeout(new IndividualRecallDisplayedFinder(raceLog).analyze());
            }
        default:
            break;
        }
    }

    @Override
    public boolean isIndividualRecallDisplayed() {
        return cachedIsIndividualRecallDisplayed;
    }

    @Override
    public void displayIndividualRecall(TimePoint displayTime) {
        raceLog.add(factory.createFlagEvent(displayTime, author, raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, true));
        scheduleIndividualRecallTimeout(displayTime);
    }

    @Override
    public void removeIndividualRecall(TimePoint timePoint) {
        if (scheduler != null) {
            scheduler.unscheduleStateEvent(new RaceStateEventImpl(timePoint, RaceStateEvents.INDIVIDUAL_RECALL_TIMEOUT));
        }
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), 
                Flags.XRAY, Flags.NONE, false));
    }

    @Override
    public boolean processStateEvent(RaceStateEvent event) {
        switch (event.getEventName()) {
        case INDIVIDUAL_RECALL_TIMEOUT:
            removeIndividualRecall(event.getTimePoint());
            return true;
        default:
            return false;
        }
    }

    @Override
    public void eventAdded(RaceLogEvent event) {
        update();
    }

    protected void update() {
        boolean isRecallDisplayed = isRecallDisplayedAnalyzer.analyze();
        if (cachedIsIndividualRecallDisplayed != isRecallDisplayed) {
            cachedIsIndividualRecallDisplayed = isRecallDisplayed;
            if (cachedIsIndividualRecallDisplayed) {
                changedListeners.onIndividualRecallDisplayed(this);
            } else {
                changedListeners.onIndividualRecallRemoved(this);
            }
        }
        
    }

    protected abstract Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime);

    @Override
    public void onStartTimeChanged(RaceState2 state) {
        if (scheduler != null && state.getStartTime() != null) {
            scheduler.scheduleStateEvents(createStartStateEvents(state.getStartTime()));
        }
    }
    
    @Override
    public void onAdvancePass(RaceState2 state) {
        unscheduleAllEvents();
    }

    private void unscheduleAllEvents() {
        if (scheduler != null) {
            scheduler.unscheduleAllEvents();
        }
    }

    private void scheduleIndividualRecallTimeout(TimePoint displayTime) {
        if (scheduler != null) {
            scheduler.scheduleStateEvents(Arrays.<RaceStateEvent> asList(new RaceStateEventImpl(displayTime
                    .plus(individualRecallRemovalTimeout), RaceStateEvents.INDIVIDUAL_RECALL_TIMEOUT)));
        }
    }
}
