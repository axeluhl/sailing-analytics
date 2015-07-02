package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;

public class RaceListViewDTO implements DTO {
    
    private LiveRacesDTO liveRaces;
    
    private ArrayList<RaceListRaceDTO> allRaces = new ArrayList<>();
    
    public void addRace(RaceListRaceDTO race) {
        // TODO implement based on race.getStart();
    }
    
    public void setLiveRaces(LiveRacesDTO liveRaces) {
        this.liveRaces = new LiveRacesDTO();
        for (int i = 0; i < liveRaces.getRaces().size(); i++) {
            LiveRaceDTO liveRace = liveRaces.getRaces().get(i);
            if (i < 3) this.liveRaces.addRace(liveRace);
            allRaces.add(convert(liveRace));
        }
    }
    
    public LiveRacesDTO getLiveRaces() {
        return liveRaces;
    }
    
    public List<RaceListRaceDTO> getAllRaces() {
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
        return raceListRace;
    }
}
