package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RaceListViewDTO implements DTO {
    
    private TreeSet<LiveRaceDTO> liveRaces = new TreeSet<>();
    
    private TreeSet<RaceListRaceDTO> finishedRaces = new TreeSet<>();
    
    public Collection<LiveRaceDTO> getLiveRaces() {
        return liveRaces;
    }
    
    public Collection<RaceListRaceDTO> getFinishedRaces() {
        return finishedRaces;
    }
    
    public Collection<RaceListSeriesDTO> getRacesForCompetitionFormat() {
        RaceListSeriesDTO withFleets = new RaceListSeriesDTO("Fleets");
        RaceListSeriesDTO noFleets = new RaceListSeriesDTO("");
        for (RaceListRaceDTO race : finishedRaces) {
            RaceListSeriesDTO series = race.getFleet() == null ? noFleets : withFleets;
            series.addRace(race);
        }
        return Arrays.asList(withFleets, noFleets);
    }
}
