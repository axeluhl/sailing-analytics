package com.sap.sailing.domain.base;

import com.sap.sailing.domain.common.Named;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

/**
 * A series is a part of a {@link Regatta}. Rounds are ordered within the regatta, and rules for who is assigned to
 * which series may exist on the regatta. For example, a regatta may have a qualification series, a final series, and a
 * medal "series" with usually only a single meda race. Each series has one or more fleets, deciding how many races per
 * race column have to be run in this round. For example, if the 49er regatta has so many competitors that they cannot
 * all start in one race, the qualification round can be split into two {@link Fleet}s, "Yellow" and "Blue," each
 * getting their separate races. Fleet assignment may or may not vary. This usually depends on the round's
 * characteristics of having ordered or unordered fleets.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Series extends Named {
    /**
     * Returns the fleets of this series, in ascending order, better fleets first.
     */
    Iterable<? extends Fleet> getFleets();
    
    /**
     * A series consists of one or more "race columns." Some people would just say "race," but we use the term "race" for
     * something that has a single start time and start line; so if each fleet in a series gets their own start for
     * something called "R2", those are as many "races" as we have fleets; therefore, we use "race column" instead to
     * describe all "races" named, e.g., "R3" in a series.
     */
    Iterable<? extends RaceColumnInSeries> getRaceColumns();
    
    RaceColumnInSeries getRaceColumnByName(String columnName);

    /**
     * Tells whether this is the "last" / "medal" race series, usually having only one race. This may have implications
     * on the scoring scheme (usually, medal races scores are doubled and cannot be discarded).
     */
    boolean isMedal();
    
    void setIsMedal(boolean isMedal);

    Fleet getFleetByName(String fleetName);

    RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry);
    
    void moveRaceColumnUp(String raceColumnName);
    
    void moveRaceColumnDown(String raceColumnName);
    
    void removeRaceColumn(String raceColumnName);
    
    Regatta getRegatta();
    
    /**
     * Sets this series' regatta.
     */
    void setRegatta(Regatta regatta);
}
