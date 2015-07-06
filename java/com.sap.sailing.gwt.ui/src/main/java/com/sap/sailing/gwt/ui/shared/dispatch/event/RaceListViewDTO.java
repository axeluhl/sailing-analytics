package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RaceListViewDTO implements DTO {
    
    private LiveRacesDTO liveRaces = new LiveRacesDTO();
    
    private TreeSet<RaceListRaceDTO> allRaces = new TreeSet<>();
    
    public void addRace(RaceListRaceDTO race) {
        allRaces.add(race);
    }
    
    public LiveRacesDTO getLiveRaces() {
        return liveRaces;
    }
    
    public Collection<RaceListRaceDTO> getAllRaces() {
        return allRaces;
    }
    
    public Collection<RaceListSeriesDTO> getRacesForCompetitionFormat() {
        RaceListSeriesDTO withFleets = new RaceListSeriesDTO("Fleets");
        RaceListSeriesDTO noFleets = new RaceListSeriesDTO("");
        for (RaceListRaceDTO race : allRaces) {
            RaceListSeriesDTO series = race.getFleet() == null ? noFleets : withFleets;
            series.addRace(race);
        }
        return Arrays.asList(withFleets, noFleets);
    }
}
