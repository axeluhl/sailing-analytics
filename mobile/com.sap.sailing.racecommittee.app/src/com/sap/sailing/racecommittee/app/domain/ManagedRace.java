package com.sap.sailing.racecommittee.app.domain;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.Named;
import com.sap.sse.common.WithID;

public interface ManagedRace extends ManagedRaceIdentifier, Named, WithID {
    
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

    /**
     * returns the list of markers to display on the racemap
     *
     * @return list of Markers
     */
    List<MapMarker> getMapMarkers();

    CourseBase getCourseOnServer();

    void setCourseOnServer(CourseBase course);

    /**
     * sets the list of competitors for a race. As the competitors are retrieved later from the backend, the list of
     * competitors has to be settable.
     * 
     * @param competitors
     *            the retrieved list of competitors for this race
     */
    void setCompetitors(Collection<Competitor> competitors);


    /**
     * sets the list of mapItems ( buoys, other boats ) for a race. As the mapItems are retrieved later from the backend, the list of
     * mapItems has to be settable.
     *
     * @param markers
     *            the retrieved list of mapItems for this race
     */
    void setMapMarkers(List<MapMarker> markers);
}