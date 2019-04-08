package com.sap.sailing.domain.abstractlog.race.state;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.CompetitorResults;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.SimpleRaceLogIdentifier;
import com.sap.sailing.domain.abstractlog.race.analyzing.impl.RaceLogResolver;
import com.sap.sailing.domain.abstractlog.race.scoring.AdditionalScoringInformationType;
import com.sap.sailing.domain.abstractlog.race.scoring.RaceLogAdditionalScoringInformationEvent;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedurePrerequisite.Resolver;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.RacingProcedurePrerequisiteAutoResolver;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.TimeRange;

/**
 * Extension to the {@link ReadonlyRaceState} allowing write-access to the state of a race.
 * 
 * All methods changing the state of the race (e.g. {@link RaceState#forceNewStartTime(TimePoint,TimePoint)}) map to a
 * single add on the underlying {@link RaceLog}. You are in charge of advancing the pass identifier (
 * {@link RaceState#setAdvancePass(TimePoint)} ) at appropriate times.
 * 
 * Additionally this ensures that there is no difference in changing the state via this interface and by
 * {@link RaceLogEvent}s added to the {@link RaceLog} by any other component. The {@link RaceState} will react the same
 * way.
 */
public interface RaceState extends ReadonlyRaceState {

    /**
     * Gets the {@link AbstractLogEventAuthor} that is used for all created {@link RaceLogEvent}s.
     */
    AbstractLogEventAuthor getAuthor();

    /**
     * Gets the currently attached {@link RacingProcedure}.
     */
    RacingProcedure getRacingProcedure();

    /**
     * Gets the currently attached {@link RacingProcedure}. Use this method to avoid casting in cases you are sure about
     * the {@link RacingProcedure}'s type.
     */
    <T extends RacingProcedure> T getTypedRacingProcedure();

    /**
     * Gets the currently attached {@link RacingProcedure}. Use this method to avoid casting in cases you are sure about
     * the {@link RacingProcedure}'s type.
     */
    <T extends RacingProcedure> T getTypedRacingProcedure(Class<T> clazz);

    /**
     * Sets the {@link RacingProcedure}. This will replace the current {@link RacingProcedure}.
     * 
     * @param now
     *            logical {@link TimePoint} the creation should be attached to.
     * @param racingProcedureType
     *            new {@link RacingProcedure} to be created-
     */
    void setRacingProcedure(TimePoint now, RacingProcedureType racingProcedureType);

    /**
     * Advances the pass of the underlying {@link RaceLog}.
     */
    void setAdvancePass(TimePoint now);

    /**
     * Forces a new start time without checking for any prerequisites to be fulfilled. Although there is no technical
     * reason for the prerequisites to be fulfilled before setting a start time event, there might be clients of this
     * {@link RaceState} that expect them to be. Therefore use this method with caution.
     * 
     * @param now
     *            logical {@link TimePoint} the start time event should be attached to.
     */
    void forceNewStartTime(TimePoint now, TimePoint startTime);
    
    /**
     * Forces a new dependent start time without checking for any prerequisites to be fulfilled. Although there is no technical
     * reason for the prerequisites to be fulfilled before setting a start time event, there might be clients of this
     * {@link RaceState} that expect them to be. Therefore use this method with caution.
     * 
     * @param now
     *            logical {@link TimePoint} the start time event should be attached to.
     * @param startTimeDifference
     *            difference in startTime to dependentRace
     * @param dependentRace
     *            Identifier of the race the startTime requested to set depends on
     */
    void forceNewDependentStartTime(TimePoint now, Duration startTimeDifference, SimpleRaceLogIdentifier dependentRace);

    /**
     * Starts a request to set a new start time. Before the start time is set the
     * {@link RacingProcedurePrerequisite.Resolver} must fulfill all prerequisites set by the currently active
     * {@link RacingProcedure}.
     * 
     * @param now
     *            logical {@link TimePoint} the start time event (and all events created due to fulfilled prerequisites)
     *            should be attached to.
     * @param startTime
     *            start time to be set
     * @param resolver
     *            Object used to fulfill all {@link RacingProcedurePrerequisite}s. If you just want to use reasonable
     *            defaults pass a {@link RacingProcedurePrerequisiteAutoResolver}.
     */
    void requestNewStartTime(TimePoint now, TimePoint startTime, RacingProcedurePrerequisite.Resolver resolver);
    
    /**
     * Starts a request to set a new dependent start time. Before the start time is set the
     * {@link RacingProcedurePrerequisite.Resolver} must fulfill all prerequisites set by the currently active
     * {@link RacingProcedure}.
     * 
     * @param now
     *            logical {@link TimePoint} the start time event (and all events created due to fulfilled prerequisites)
     *            should be attached to.
     * @param startTimeDifference
     *            difference in startTime to dependentRace
     * @param dependentRace
     *            Identifier of the race the startTime requested to set depends on
     * @param raceLogResolver
     *            Needed in order to fetch the RaceLog of the dependentRace
     * @param resolver
     *            Object used to fulfill all {@link RacingProcedurePrerequisite}s. If you just want to use reasonable
     *            defaults pass a {@link RacingProcedurePrerequisiteAutoResolver}.
     */
    void requestNewDependentStartTime(TimePoint now, Duration startTimeDifference, SimpleRaceLogIdentifier dependentRace, RaceLogResolver raceLogResolver,
            Resolver resolver);

    /**
     * Sets the finishing time.
     */
    void setFinishingTime(TimePoint now);

    /**
     * Sets the finished time.
     */
    void setFinishedTime(TimePoint now);

    /**
     * Sets the protest time.
     */
    void setProtestTime(TimePoint now, TimeRange protestTime);

    /**
     * Signals the abort of this race.
     * 
     * @param isPostponed
     *            indicates whether the abort is caused by a postponment of the race (rather than a full abort)
     * @param reasonFlag
     *            instance of {@link Flags} explaining the abort.
     */
    void setAborted(TimePoint now, boolean isPostponed, Flags reasonFlag);

    /**
     * Signals general recall.
     */
    void setGeneralRecall(TimePoint now);

    /**
     * Sets a new unconfirmed finishing list.
     */
    void setFinishPositioningListChanged(TimePoint now, CompetitorResults positionedCompetitors);

    /**
     * Sets a new confirmed finishing list.
     */
    void setFinishPositioningConfirmed(TimePoint now, CompetitorResults positionedCompetitors);

    /**
     * Sets a new active course design.
     * 
     * @param courseDesignerMode
     *            the type of course designer through which the course was created; this decides about whether the
     *            waypoint specification will be considered at all. For example, the "By Marks" course designer does not
     *            produce a valid waypoints list which therefore must be ignored instead of using it to update a
     *            TrackedRace's course.
     */
    void setCourseDesign(TimePoint now, CourseBase courseDesign, CourseDesignerMode courseDesignerMode);

    /**
     * Enters a new wind fix for this race.
     * @param isMagnetic TODO
     */
    void setWindFix(TimePoint now, Wind wind, boolean isMagnetic);
    
    /**
     * Marks this race state with a new {@link RaceLogAdditionalScoringInformationEvent} or revokes
     * an already existing one.
     */
    void setAdditionalScoringInformationEnabled(TimePoint creationTimePoint, boolean enable, AdditionalScoringInformationType informationType);
    
    boolean isAdditionalScoringInformationEnabled(AdditionalScoringInformationType informationType);

    /**
     * forces a RaceStateUpdate
     */
    void forceUpdate();
}
