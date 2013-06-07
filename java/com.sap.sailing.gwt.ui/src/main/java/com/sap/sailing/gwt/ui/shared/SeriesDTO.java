package com.sap.sailing.gwt.ui.shared;

import java.util.List;

import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;

public class SeriesDTO extends NamedDTO {
    private static final long serialVersionUID = -3813445377426310687L;
    private List<FleetDTO> fleets;
    private List<RaceColumnDTO> raceColumns;
    private boolean isMedal;
    private int[] discardThresholds;
    
    public SeriesDTO() {}
    
    public SeriesDTO(String name, List<FleetDTO> fleets, List<RaceColumnDTO> raceColumns, boolean isMedal, int[] discardThresholds) {
        super(name);
        this.fleets = fleets;
        this.raceColumns = raceColumns;
        this.isMedal = isMedal;
        this.discardThresholds = discardThresholds;
    }
    
    public boolean isMedal() {
        return isMedal;
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

    public void setFleets(List<FleetDTO> fleets) {
        this.fleets = fleets;
    }

    public void setMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }

    public List<RaceColumnDTO> getRaceColumns() {
        return raceColumns;
    }

    public void setRaceColumns(List<RaceColumnDTO> raceColumns) {
        this.raceColumns = raceColumns;
    }
    
    /**
     * @return whether this series defines its local result discarding rule; if so, any leaderboard based on the
     *         enclosing regatta has to respect this and has to use a result discarding rule implementation that keeps
     *         discards local to each series rather than spreading them across the entire leaderboard.
     */
    public boolean definesSeriesDiscardThresholds() {
        return getDiscardThresholds() != null;
    }

    public int[] getDiscardThresholds() {
        return discardThresholds;
    }
    
    public void setDiscardThresholds(int[] discardThresholds) {
        this.discardThresholds = discardThresholds;
    }
}
