package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogChangedListener;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventFactory;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishedTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.FinishingTimeFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.IndividualRecallDisplayedFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.IndividualRecallRemovedFinder;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.IsFinishedAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.IsInFinishingPhaseAnalyzer;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.IsIndividualRecallDisplayedAnalyzer;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogChangedVisitor;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEventScheduler;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEventImpl;
import com.sap.sailing.domain.abstractlog.race.state.impl.RaceStateEvents;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;

/**
 * Base class for all your {@link RacingProcedure}s.
 * 
 * It is a good idea to call {@link BaseRacingProcedure#update()} in your constructor to ensure your procedure is
 * correctly initialized.
 * 
 */
public abstract class BaseRacingProcedure extends BaseRaceStateChangedListener implements RacingProcedure,
        RaceLogChangedListener {

    private final static long individualRecallRemovalTimeout = 4 * 60 * 1000; // minutes * seconds * milliseconds

    private RaceStateEventScheduler scheduler;

    protected final RaceLog raceLog;
    protected final AbstractLogEventAuthor author;
    protected final RaceLogEventFactory factory;
    private final RacingProcedureConfiguration configuration;

    private final RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> changedListeners;
    private final IsIndividualRecallDisplayedAnalyzer isRecallDisplayedAnalyzer;
    private final IndividualRecallDisplayedFinder recallDisplayedFinder;
    private final IndividualRecallRemovedFinder recallRemovedFinder;
    private final FinishingTimeFinder finishingTimeFinder;
    private final FinishedTimeFinder finishedTimeFinder;
    private final RaceLogEventVisitor raceLogListener;

    private boolean cachedIsIndividualRecallDisplayed;

    /**
     * When calling me, call {@link BaseRacingProcedure#update()} afterwards!
     */
    public BaseRacingProcedure(RaceLog raceLog, AbstractLogEventAuthor author, RaceLogEventFactory factory,
            RacingProcedureConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration must not be null");
        }
        
        this.raceLog = raceLog;
        this.author = author;
        this.factory = factory;
        this.configuration = configuration;

        this.changedListeners = createChangedListenerContainer();
        this.isRecallDisplayedAnalyzer = new IsIndividualRecallDisplayedAnalyzer(raceLog);
        this.recallDisplayedFinder = new IndividualRecallDisplayedFinder(raceLog);
        this.recallRemovedFinder = new IndividualRecallRemovedFinder(raceLog);
        this.finishingTimeFinder = new FinishingTimeFinder(raceLog);
        this.finishedTimeFinder = new FinishedTimeFinder(raceLog);

        this.raceLogListener = new RaceLogChangedVisitor(this);
        this.raceLog.addListener(raceLogListener);

        this.cachedIsIndividualRecallDisplayed = false;
    }
    
    @Override
    public void detach() {
        raceLog.removeListener(raceLogListener);
        changedListeners.removeAll();
    }
    
    @Override
    public void addChangedListener(RacingProcedureChangedListener listener) {
        changedListeners.addBaseListener(listener);
    }
    
    public void removeChangedListener(RacingProcedureChangedListener listener) {
        changedListeners.remove(listener);
    };

    protected RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> getChangedListeners() {
        return changedListeners;
    }

    protected abstract RacingProcedureChangedListeners<? extends RacingProcedureChangedListener> createChangedListenerContainer();

    @Override
    public RaceLog getRaceLog() {
        return raceLog;
    }

    @Override
    public void setStateEventScheduler(RaceStateEventScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void triggerStateEventScheduling(ReadonlyRaceState state) {
        switch (state.getStatus()) {
        case SCHEDULED:
        case STARTPHASE:
            onStartTimeChanged(state);
            break;
        case RUNNING:
            if (isIndividualRecallDisplayed()) {
                rescheduleIndividualRecallTimeout(recallDisplayedFinder.analyze());
            }
            break;
        default:
            break;
        }
    }

    @Override
    public boolean isIndividualRecallDisplayed() {
        return cachedIsIndividualRecallDisplayed;
    }

    @Override
    public boolean isIndividualRecallDisplayed(TimePoint at) {
        if (hasIndividualRecall()) {
            return new IsIndividualRecallDisplayedAnalyzer(getRaceLog(), at).analyze();
        }
        return false;
    }
    
    @Override
    public TimePoint getIndividualRecallDisplayedTime() {
        if (hasIndividualRecall()) {
            return recallDisplayedFinder.analyze();
        }
        return null;
    }
    
    @Override
    public TimePoint getIndividualRecallRemovalTime() {
        TimePoint displayed = getIndividualRecallDisplayedTime();
        if (hasIndividualRecall()) {
            TimePoint removedEvent = recallRemovedFinder.analyze();
            if (removedEvent != null && (displayed == null || removedEvent.after(displayed))) {
                return removedEvent;
            }
        }
        if (displayed != null) {
            return displayed.plus(individualRecallRemovalTimeout);
        }
        return null;
    }

    @Override
    public void displayIndividualRecall(TimePoint displayTime) {
        raceLog.add(factory.createFlagEvent(displayTime, author, raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, true));
    }

    @Override
    public void removeIndividualRecall(TimePoint timePoint) {
        raceLog.add(factory.createFlagEvent(timePoint, author, raceLog.getCurrentPassId(), Flags.XRAY, Flags.NONE, false));
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
    public boolean hasIndividualRecall() {
        return configuration.hasInidividualRecall() == null ? 
                hasIndividualRecallByDefault() : configuration.hasInidividualRecall();
    }

    protected abstract boolean hasIndividualRecallByDefault();

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
                rescheduleIndividualRecallTimeout(recallDisplayedFinder.analyze());
            } else {
                changedListeners.onIndividualRecallRemoved(this);
                unscheduleStateEvent(RaceStateEvents.INDIVIDUAL_RECALL_TIMEOUT);
            }
        }    
        
        // always call listeners for changed flag, as this does not only affect recall, but also
        // changes start procedure, for which some text elements need to be updated
        // ({@link BaseRaceInfoFragmen#renderFlagChangesCountdown}
        changedListeners.onActiveFlagsChanged(this);
    }

    private void rescheduleIndividualRecallTimeout(TimePoint displayTime) {
        unscheduleStateEvent(RaceStateEvents.INDIVIDUAL_RECALL_TIMEOUT);
        scheduleStateEvents(new RaceStateEventImpl(displayTime.plus(individualRecallRemovalTimeout), RaceStateEvents.INDIVIDUAL_RECALL_TIMEOUT));
    }
    
    protected void scheduleStateEvents(RaceStateEvent stateEvent) {
        scheduleStateEvents(Arrays.asList(stateEvent));
    }
    
    protected void scheduleStateEvents(Collection<RaceStateEvent> stateEvents) {
        if (scheduler != null) {
            scheduler.scheduleStateEvents(stateEvents);
        }
    }
    
    protected void unscheduleStateEvent(RaceStateEvents raceStateEventName) {
        if (scheduler != null) {
            scheduler.unscheduleStateEvent(raceStateEventName);
        }
    }
    
    @Override
    public RacingProcedureConfiguration getConfiguration() {
        return configuration;
    }

    protected abstract Collection<RaceStateEvent> createStartStateEvents(TimePoint startTime);

    @Override
    public void onStartTimeChanged(ReadonlyRaceState state) {
        getChangedListeners().onActiveFlagsChanged(this);
        if (scheduler != null && state.getStartTime() != null) {
            scheduler.unscheduleAllEvents();
            scheduler.scheduleStateEvents(createStartStateEvents(state.getStartTime()));
        }
    }
    
    @Override
    public void onAdvancePass(ReadonlyRaceState state) {
        unscheduleAllEvents();
        update();
    }

    private void unscheduleAllEvents() {
        if (scheduler != null) {
            scheduler.unscheduleAllEvents();
        }
    }
    
    protected boolean isFinished(TimePoint at) {
        IsFinishedAnalyzer analyzer = new IsFinishedAnalyzer(raceLog, finishedTimeFinder, at);
        return analyzer.analyze();
    }
    
    protected boolean isInFinishingPhase(TimePoint at) {
        IsInFinishingPhaseAnalyzer analyzer = new IsInFinishingPhaseAnalyzer(raceLog, finishingTimeFinder, at);
        return analyzer.analyze();
    }
    
    protected TimePoint getFinishingTime() {
        return finishingTimeFinder.analyze();
    }
    
    protected TimePoint getFinishedTime() {
        return finishedTimeFinder.analyze();
    }
}
