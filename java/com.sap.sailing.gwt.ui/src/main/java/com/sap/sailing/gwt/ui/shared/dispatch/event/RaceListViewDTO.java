package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.dto.CompetitorDTOImpl;
import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;

public class RaceListViewDTO implements DTO {
    
    private LiveRacesDTO liveRaces = new LiveRacesDTO();
    
    private ArrayList<RaceListRaceDTO> allRaces = new ArrayList<>();
    
    public void addRace(RaceListRaceDTO race) {
        // TODO implement based on race.getStart();
    }
    
    public LiveRacesDTO getLiveRaces() {
        return liveRaces;
    }
    
    public List<RaceListRaceDTO> getAllRaces() {
        ArrayList<RaceListRaceDTO> list = new ArrayList<RaceListRaceDTO>(liveRaces.getRaces().size());
        for (LiveRaceDTO liveRace : liveRaces.getRaces()) {
            list.add(convert(liveRace));
        }
        return list;
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
    
    private RaceListRaceDTO convert(RaceMetadataDTO liveRace) {
        RaceListRaceDTO raceListRace = new RaceListRaceDTO(liveRace.getRegattaName(), liveRace.getRaceName());
        raceListRace.setBoatClass(liveRace.getBoatClass());
        raceListRace.setCourse(liveRace.getCourse());
        raceListRace.setCourseArea(liveRace.getCourseArea());
        raceListRace.setFleet(liveRace.getFleet());
        raceListRace.setRegattaDisplayName(liveRace.getRegattaDisplayName());
        raceListRace.setStart(liveRace.getStart());
        raceListRace.setTrackedRaceName(liveRace.getTrackedRaceName());
        raceListRace.setTrackingState(liveRace.getTrackingState());
        raceListRace.setViewState(liveRace.getViewState());
        raceListRace.setWind(liveRace.getWind());
        raceListRace.setWinner(new CompetitorDTOImpl("Ingmar Pedersen", null, null, "de", 
                null, null, null, null, null, null, null, null, null));
        return raceListRace;
    }
}
