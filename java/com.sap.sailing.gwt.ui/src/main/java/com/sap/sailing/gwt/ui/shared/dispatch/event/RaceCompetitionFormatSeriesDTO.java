package com.sap.sailing.gwt.ui.shared.dispatch.event;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.gwt.ui.shared.dispatch.DTO;

public class RaceCompetitionFormatSeriesDTO implements DTO {

    private String seriesName;
    private int raceCount = 0;
    private int competitorCount = 0;
    private ArrayList<RaceCompetitionFormatFleetDTO> fleets = new ArrayList<>();

    protected RaceCompetitionFormatSeriesDTO() {
    }

    public RaceCompetitionFormatSeriesDTO(String seriesName) {
        this.seriesName = seriesName;
    }
    
    public void addFleet(RaceCompetitionFormatFleetDTO fleet) {
        fleets.add(fleet);
        raceCount += fleet.getRaces().size();
    }
    
    public void setCompetitorCount(int competitorCount) {
        this.competitorCount = competitorCount;
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
        return fleets;
    }

}
