package com.sap.sailing.racecommittee.app.domain.state;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedure;
import com.sap.sailing.racecommittee.app.domain.startprocedure.StartProcedureRaceStateChangedListener;

public interface RaceState extends StartProcedureRaceStateChangedListener {

    /**
     * Register a new listener on state changes.
     * 
     * @param listener
     *            to be registered.
     */
    void registerListener(RaceStateChangedListener listener);

    /**
     * Unregister a previously registered state change listener.
     * 
     * @param listener
     *            to be unregistered.
     */
    void unregisterListener(RaceStateChangedListener listener);

    /**
     * @return the log of the race.
     */
    RaceLog getRaceLog();

    /**
     * Gets the current race's start time.
     * 
     * @return the start time or <code>null</code>.
     */
    TimePoint getStartTime();
    
    /**
     * Gets the current race's finished time.
     * 
     * @return the finished time or <code>null</code>.
     */
    TimePoint getFinishedTime();
    
    /**
     * Returns the time of the first finisher for the current race or when the blue flag is displayed.
     * 
     * @return the first finisher time or <code>null</code>.
     */
    TimePoint getFinishingStartTime();
    
    /**
     * Returns the current race's course design
     * 
     * @return the course data or <code>null</code>.
     */
    CourseBase getCourseDesign();
    
    /**
     * Returns the current race's finish positioning list
     * 
     * @return the positioning list for the finish or <code>null</code>.
     */
    List<Triple<Serializable, String, MaxPointsReason>> getFinishPositioningList();

    /**
     * Sets the current race's start time
     * 
     * @param newStartTime
     *            to be set.
     */
    void setStartTime(TimePoint newStartTime);
    
    /**
     * Sets the current race's course design
     * 
     * @param new course data
     *            to be set.
     */
    void setCourseDesign(CourseBase newCourseData);
    
    /**
     * Sets the current finish positioning list for the race
     * 
     * @param positionedCompetitors the current finishing list
     */
    void setFinishPositioningListChanged(List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors);
    
    /**
     * Sets a confirmation event for the finish positionings
     */
    void setFinishPositioningConfirmed();

    /**
     * Updates the race's status.
     * 
     * @return the new status, as returned by {@link RaceState#getStatus()}.
     */
    RaceLogRaceStatus updateStatus();

    /**
     * @return the status of the race.
     */
    RaceLogRaceStatus getStatus();
    
    /**
     * @return the start procedure of the race
     */
    StartProcedure getStartProcedure();

    /**
     * @return the timepoint at which the individual recall was displayed or null when the individual recall is already removed
     */
    TimePoint getIndividualRecallDisplayedTime();

    String getPathfinder();

    void setPathfinder(String sailingId);

}
