package com.sap.sailing.domain.abstractlog.race.state.racingprocedure;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEvent;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEventProcessor;
import com.sap.sailing.domain.abstractlog.race.state.RaceStateEventScheduler;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.TimePoint;

/**
 * Read-only interface for every racing procedure. A racing procedure defines the starting sequence of a race various
 * other properties regarding how a race is organized.
 * 
 * A {@link ReadonlyRaceState} is always attached to its {@link RaceState}.
 * 
 * The state of a {@link ReadonlyRacingProcedure} is driven by the {@link RaceLog} and {@link RaceLogAnalyzer}, see
 * {@link ReadonlyRaceState} for a more in-detail description.
 * 
 * See {@link ReadonlyRaceState} for an explanation on
 * {@link ReadonlyRacingProcedure#setStateEventScheduler(RaceStateEventScheduler)}.
 * 
 */
public interface ReadonlyRacingProcedure extends RaceStateEventProcessor, RaceStateChangedListener {

    /**
     * Accesses the underlying {@link RaceLog}.
     */
    RaceLog getRaceLog();

    /**
     * Gets the {@link RacingProcedureType}.
     */
    RacingProcedureType getType();

    /**
     * Gets the {@link RacingProcedure}'s configuration.
     */
    RacingProcedureConfiguration getConfiguration();

    /**
     * Adds a {@link RacingProcedureChangedListener}.
     */
    void addChangedListener(RacingProcedureChangedListener listener);

    /**
     * Removes a {@link RacingProcedureChangedListener}.
     */
    void removeChangedListener(RacingProcedureChangedListener listener);

    /**
     * Returns <code>true</code> if the start phase of this {@link ReadonlyRacingProcedure is active given the current
     * time and start time.
     * 
     * @param startTime
     *            start time to be used to determine whether the start phase is active or not. Must not be {@code null}
     * @param now
     *            {@link TimePoint} used as the current time on determining whether the start phase is active or not.
     */
    boolean isStartphaseActive(TimePoint startTime, TimePoint now);

    /**
     * Returns <code>true</code> if this {@link ReadonlyRacingProcedure} should allow signaling individual recalls.
     */
    boolean hasIndividualRecall();

    /**
     * Returns {@code true} if this racing procedure allows users to enter scores in the app
     */
    boolean isResultEntryEnabled();

    /**
     * Returns <code>true</code> if there is an individual recall signaled in the {@link RaceLog} right now.
     */
    boolean isIndividualRecallDisplayed();

    /**
     * Returns <code>true</code> if there is an individual recall signaled in the {@link RaceLog} at the TimePoint {@code at}.
     */
    boolean isIndividualRecallDisplayed(TimePoint at);

    /**
     * Gets the time the individual recall flag was displayed (or <code>null</code>).
     */
    TimePoint getIndividualRecallDisplayedTime();

    /**
     * Gets the time the individual recall flag was or shall be removed (or <code>null</code> if it hasn't been set or no
     * race start time is known). When the flag is currently set, the removal time can either be specified by an explicit
     * race log event or is automatically triggered four minutes after the race start time.
     */
    TimePoint getIndividualRecallRemovalTime();

    /**
     * Gets the {@link FlagPoleState} active for the given start and current time. 
     */
    FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now);

    /**
     * Sets the {@link RaceStateEventScheduler} which drives automatic events triggering calls.
     * 
     * This method is called by the framework. You don't need to call this method.
     */
    void setStateEventScheduler(RaceStateEventScheduler scheduler);

    /**
     * This method is used by the framework to ensure that all automatic events are triggered when a
     * {@link ReadonlyRacingProcedure} is attached to its {@link RaceState}. You don't need to call this method.
     */
    void triggerStateEventScheduling(ReadonlyRaceState state);

    /**
     * Returns a {@link RacingProcedurePrerequisite} object that should be resolved by
     * {@link RacingProcedurePrerequisite.Resolver} to ensure that everything is set up correctly before the start of
     * the race.
     * 
     * This method is called by the parent {@link RaceState}. You don't need to call this method.
     */
    RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint now, TimePoint startTime,
            RacingProcedurePrerequisite.FulfillmentFunction function);


    /**
     * Delivers the time points and types of the events around the start, each optionally
     * leading to a change in race status.
     */
    Iterable<RaceStateEvent> createStartStateEvents(TimePoint startTime);

    /**
     * This method is called by the framework. You don't need to call it.
     */
    void detach();
}
