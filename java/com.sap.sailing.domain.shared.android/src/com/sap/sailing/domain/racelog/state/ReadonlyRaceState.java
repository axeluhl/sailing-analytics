package com.sap.sailing.domain.racelog.state;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.analyzing.impl.RaceLogAnalyzer;
import com.sap.sailing.domain.racelog.impl.RaceLogEventComparator;
import com.sap.sailing.domain.racelog.impl.RaceLogImpl;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sse.common.TimePoint;

/**
 * This interface gives you reading access to the state of a race. You can query various properties of the race
 * including but not limited to its start time, finished time and its current course design.
 * 
 * There is always a {@link ReadonlyRacingProcedure} attached. This object decides on various aspects of the race,
 * including the starting sequence.
 * 
 * Types implementing {@link ReadonlyRaceState} will monitor the underlying {@link RaceLog} all the time and will change
 * according to the contents of it. Due to the use of {@link RaceLogAnalyzer}s for this, the current state is derived
 * from the current valid {@link RaceLogEvent}s defined by the validity implementation of {@link RaceLogImpl} and the
 * ordering of the {@link RaceLogEventComparator}. For example for a {@link RaceLog} containing a high-priority
 * {@link RaceLogStartTimeEvent} the {@link ReadonlyRaceState} won't trigger or signal a change when a low-priority
 * start time is added later on.
 * 
 * When you want to use this interface for a short period of time, just use the getters to query the state of the race's
 * properties. On the other it is good practice to let the interface do the work by registering a
 * {@link RaceStateChangedListener} via {@link ReadonlyRaceState#addChangedListener(RaceStateChangedListener)}. Your
 * listener will be called whenever any of the race's property changes.
 * 
 * When doing so it is a good idea to set a {@link RaceStateEventScheduler} via
 * {@link ReadonlyRaceState#setStateEventScheduler(RaceStateEventScheduler)}. This way the {@link ReadonlyRaceState} is
 * able to inform you about automatic timer-driven changes like a change in the displayed flags. Having a
 * {@link RaceStateEventScheduler} is not mandatory for the state to function properly.
 * 
 */
public interface ReadonlyRaceState extends RaceStateEventProcessor {

    /**
     * Accesses the underlying {@link RaceLog}.
     */
    RaceLog getRaceLog();

    /**
     * Gets the currently attached {@link ReadonlyRacingProcedure}.
     */
    ReadonlyRacingProcedure getRacingProcedure();

    /**
     * Gets the currently attached {@link ReadonlyRacingProcedure}. Use this method to avoid casting in cases you are
     * sure about the {@link ReadonlyRacingProcedure}'s type.
     */
    <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure();

    /**
     * Gets the currently attached {@link ReadonlyRacingProcedure}. Use this method to avoid casting in cases you are
     * sure about the {@link ReadonlyRacingProcedure}'s type.
     */
    <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure(Class<T> clazz);

    /**
     * Sets the {@link RaceStateEventScheduler} which drives automatic events triggering calls to the registered
     * {@link RaceStateEventScheduler}s.
     */
    void setStateEventScheduler(RaceStateEventScheduler scheduler);

    /**
     * Gets the {@link RegattaConfiguration} that is used for this race.
     */
    RegattaConfiguration getConfiguration();

    /**
     * Adds a {@link RaceStateChangedListener}.
     */
    void addChangedListener(RaceStateChangedListener listener);

    /**
     * Removes the {@link RaceStateChangedListener}.
     */
    void removeChangedListener(RaceStateChangedListener listener);

    /**
     * Gets the races overall status.
     */
    RaceLogRaceStatus getStatus();

    /**
     * If there is a start time set for the current pass, returns it. Otherwise <code>null</code>.
     */
    TimePoint getStartTime();

    /**
     * If there is a finishing time set for the current pass, returns it. Otherwise <code>null</code>.
     */
    TimePoint getFinishingTime();

    /**
     * If there is a finished time set for the current pass, returns it. Otherwise <code>null</code>.
     */
    TimePoint getFinishedTime();

    /**
     * If there is a (unconfirmed) finish positioning list set for the current pass, returns the most recent one.
     * Otherwise <code>null</code>.
     */
    CompetitorResults getFinishPositioningList();

    /**
     * If there is a (confirmed) finish positioning list set for the current pass, returns the most recent one.
     * Otherwise <code>null</code>.
     */
    CompetitorResults getConfirmedFinishPositioningList();

    /**
     * If there is a protest time set, returns the most recent one. Otherwise <code>null</code>.
     */
    TimePoint getProtestTime();

    /**
     * If there is a course set, returns the most recent one. Otherwise <code>null</code>.
     */
    CourseBase getCourseDesign();

    /**
     * If wind has been entered, returns the most recent one. Otherwise <code>null</code>.
     */
    Wind getWindFix();
}
