package com.sap.sailing.domain.base;

import com.sap.sailing.domain.racelog.RaceLogStore;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;

/**
 * A series is a part of a {@link Regatta}. Rounds are ordered within the regatta, and rules for who is assigned to
 * which series may exist on the regatta. For example, a regatta may have a qualification series, a final series, and a
 * medal "series" with usually only a single medal race. Each series has one or more fleets, deciding how many races per
 * race column have to be run in this round. For example, if the 49er regatta has so many competitors that they cannot
 * all start in one race, the qualification round can be split into two {@link Fleet}s, "Yellow" and "Blue," each
 * getting their separate races. Fleet assignment may or may not vary. This usually depends on the round's
 * characteristics of having ordered or unordered fleets.<p>
 * 
 * To receive notifications when {@link TrackedRace tracked races} are linked to or unlinked from any of this series'
 * columns, {@link RaceColumnListener}s can be added / removed.
 * 
 * @author Axel Uhl (D043530)
 * 
 */
public interface Series extends SeriesData {
    
    RaceColumnInSeries getRaceColumnByName(String columnName);
    
    void setIsMedal(boolean isMedal);

    Fleet getFleetByName(String fleetName);

    RaceColumnInSeries addRaceColumn(String raceColumnName, TrackedRegattaRegistry trackedRegattaRegistry, RaceLogStore raceLogStore);
    
    void moveRaceColumnUp(String raceColumnName);
    
    void moveRaceColumnDown(String raceColumnName);
    
    void removeRaceColumn(String raceColumnName);
    
    Regatta getRegatta();
    
    /**
     * Sets this series' regatta.
     */
    void setRegatta(Regatta regatta);
    
    void addRaceColumnListener(RaceColumnListener listener);
    
    void removeRaceColumnListener(RaceColumnListener listener);
}
