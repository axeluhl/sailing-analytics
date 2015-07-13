package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.dispatch.regatta.RegattaWithProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;

public class RaceListViewDTO implements DTO {
    
    private TreeSet<LiveRaceDTO> liveRaces = new TreeSet<>();
    
    private TreeSet<RaceListRaceDTO> finishedRaces = new TreeSet<>();
    
    private RegattaWithProgressDTO progress;
    
    public void add(LiveRaceDTO liveRace) {
        add(liveRaces, liveRace);
    }
    
    public void add(RaceListRaceDTO finishedRace) {
        add(finishedRaces, finishedRace);
    }
    
    private <T extends RaceMetadataDTO> void add(TreeSet<T> set, T race) {
        if (race != null) {
            set.add(race);
        }
    }
    
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

    public RegattaWithProgressDTO getProgress() {
        return progress;
    }

    public void setProgress(RegattaWithProgressDTO progress) {
        this.progress = progress;
    }
}
