package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.Collection;
import java.util.TreeMap;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleRaceMetadataDTO;

public class RaceCompetitionFormatSeriesDTO implements DTO {

    private String seriesName;
    private int raceCount = 0;
    private int competitorCount = (int) (Math.random() * 10); // TODO
    private TreeMap<FleetMetadataDTO, RaceCompetitionFormatFleetDTO> fleets = new TreeMap<>();

    protected RaceCompetitionFormatSeriesDTO() {
    }

    public RaceCompetitionFormatSeriesDTO(String seriesName) {
        this.seriesName = seriesName;
    }

    public void addRace(FleetMetadataDTO fleetMetadata, SimpleRaceMetadataDTO race) {
        RaceCompetitionFormatFleetDTO fleet = ensureFleet(fleetMetadata);
        fleet.addRace(race);
        raceCount++;
    }

    private RaceCompetitionFormatFleetDTO ensureFleet(FleetMetadataDTO fleetData) {
        RaceCompetitionFormatFleetDTO fleet = fleets.get(fleetData);
        if (fleet == null) {
            fleets.put(fleetData, fleet = new RaceCompetitionFormatFleetDTO(fleetData));
        }
        return fleet;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public int getRaceCount() {
        return raceCount;
    }

    public int getCompetitorCount() {
        return competitorCount;
    }

    public Collection<RaceCompetitionFormatFleetDTO> getFleets() {
        return fleets.values();
    }

}
