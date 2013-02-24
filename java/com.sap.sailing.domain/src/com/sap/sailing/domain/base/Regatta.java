package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.WithID;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegatta;

/**
 * The name shall be unique across all regattas tracked concurrently. In particular, if you want to
 * keep apart regattas in different boat classes, make sure the boat class name becomes part of the
 * regatta name.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface Regatta extends Named, WithID {
	BoatClass getBoatClass();
	
	/**
     * A regatta name may be composed, e.g., from an overall regatta name and the boat class name. A factory or constructor
     * may require the base name to which the boat class name will be appended. This method emits the base name.
     */
    String getBaseName();
	
    ScoringScheme getScoringScheme();
    
    CourseArea getDefaultCourseArea();
    
    /**
     * A regatta consists of one or more series.
     * 
     * @return an unmodifiable iterable sequence of the series of which this regatta consists.
     */
    Iterable<? extends Series> getSeries();
    
    /**
     * @return the first series from {@link #getSeries} whose {@link Series#getName() name} equals
     *         <code>seriesName<code>,
     * or <code>null</code> if no such series exists
     */
    Series getSeriesByName(String seriesName);

    /**
     * Please note that the {@link RaceDefinition}s of the {@link Regatta} are not necessarily in sync with the
     * {@link TrackedRace}s of the {@link TrackedRegatta} whose {@link TrackedRegatta#getRegatta() regatta} is this regatta.
     * For example, it may be the case that a {@link RaceDefinition} is returned by this method for which no
     * {@link TrackedRace} exists in the corresponding {@link TrackedRegatta}. This could be the case, e.g., during
     * the initialization of the tracker as well as during removing a race from the server.<p>
     */
    Iterable<RaceDefinition> getAllRaces();
    
    /**
     * Please note that the set of {@link RaceDefinition}s contained by this regatta may not match up with the 
     * {@link TrackedRace}s of the {@link TrackedRegatta} corresponding to this regatta. See also {@link #getAllRaces()}.
     * 
     * @return <code>null</code>, if this regatta does not contain a race (see {@link #getAllRaces}) whose
     * {@link RaceDefinition#getName()} equals <code>raceName</code>
     */
    RaceDefinition getRaceByName(String raceName);
    
    Iterable<Competitor> getCompetitors();

    void addRace(RaceDefinition race);

    void removeRace(RaceDefinition raceDefinition);
 
    void addRegattaListener(RegattaListener listener);
    
    void removeRegattaListener(RegattaListener listener);

    RegattaIdentifier getRegattaIdentifier();

    /**
     * Regattas may be constructed as implicit default regattas in which case they won't need to be stored
     * durably and don't contain valuable information worth being preserved; or they are constructed explicitly
     * with series and race columns in which case this data needs to be protected. This flag indicates whether
     * the data of this regatta needs to be maintained persistently.
     */
    boolean isPersistent();

    void addRaceColumnListener(RaceColumnListener listener);

    void removeRaceColumnListener(RaceColumnListener listener);
}
