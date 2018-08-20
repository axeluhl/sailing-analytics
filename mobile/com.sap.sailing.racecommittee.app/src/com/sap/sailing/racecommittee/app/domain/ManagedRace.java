package com.sap.sailing.racecommittee.app.domain;

import java.util.Collection;
import java.util.Map;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.racegroup.FilterableRace;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.domain.impl.Result;
import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.TimePoint;

/**
 * A managed race's {@link #getName()} is the race column's name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface ManagedRace extends FilterableRace, ManagedRaceIdentifier, NamedWithID {

    /**
     * @return the identifier of the race.
     */
    ManagedRaceIdentifier getIdentifier();

    /**
     * @return the state of the race.
     */
    RaceState getState();

    /**
     * Shortcut to {@link RaceState#getRaceLog()} of {@link ManagedRace#getState()}.
     *
     * @return the log of the race.
     */
    RaceLog getRaceLog();

    /**
     * Shortcut to {@link RaceState#getStatus()} of {@link ManagedRace#getState()}.
     *
     * @return the status of the race's state.
     */
    RaceLogRaceStatus getStatus();

    /**
     * the current course of the race
     *
     * @return the course of the race
     */
    CourseBase getCourseDesign();

    /**
     * returns the list of competitors for this race
     *
     * @return list of competitors
     */
    Collection<Competitor> getCompetitors();

    Map<Competitor, Boat> getCompetitorsAndBoats();
    
    CourseBase getCourseOnServer();

    void setCourseOnServer(CourseBase course);

    /**
     * sets the list of competitors and boats for a race. As the competitors are retrieved later from the backend, the list of
     * competitors has to be settable.
     *
     * @param competitors
     *            the retrieved list of competitors for this race
     */
    void setCompetitors(Map<Competitor, Boat> competitorsAndBoats);

    /**
     * Returns true if {@link RaceState} has been calculated and set
     *
     * @return true, if {@link RaceState} has been set
     */
    boolean calculateRaceState();

    /**
     * sets the finished time, if the finished time is after the finishing time; check the {@link Result} for error message
     *
     * @param finishedTime finished time
     * @return result object
     */
    Result setFinishedTime(TimePoint finishedTime);

    /**
     * sets the finishing time, if the finishing time is after the start time; check the {@link Result} for error message
     *
     * @param finishingTime finishing time
     * @return result object
     */
    Result setFinishingTime(TimePoint finishingTime);

    /**
     * @return
     */
    double getFactor();

    /**
     * @return factor
     */
    Double getExplicitFactor();

    /**
     * @param factor
     */
    void setExplicitFactor(Double factor);
}