package com.sap.sailing.gwt.ui.shared;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sse.security.shared.dto.NamedDTO;

public class SeriesDTO extends NamedDTO {
    private static final long serialVersionUID = -3813445377426310687L;
    private List<FleetDTO> fleets;
    private List<RaceColumnDTO> raceColumns;
    private boolean isMedal;
    private boolean isFleetsCanRunInParallel;
    private int[] discardThresholds;
    private Boolean startsWithZeroScore;
    private boolean firstColumnIsNonDiscardableCarryForward;
    private boolean hasSplitFleetContiguousScoring;
    private Integer maximumNumberOfDiscards;
    
    public SeriesDTO() {}
    
    public SeriesDTO(String name, List<FleetDTO> fleets, List<RaceColumnDTO> raceColumns, boolean isMedal, boolean isFleetsCanRunInParallel,
            int[] discardThresholds, boolean startsWithZeroScore, boolean firstColumnIsNonDiscardableCarryForward,
            boolean hasSplitFleetContiguousScoring, Integer maximumNumberOfDiscards) {
        super(name);
        this.fleets = fleets;
        this.raceColumns = raceColumns;
        this.isMedal = isMedal;
        this.isFleetsCanRunInParallel = isFleetsCanRunInParallel;
        this.startsWithZeroScore = startsWithZeroScore;
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
        this.discardThresholds = discardThresholds;
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
        this.maximumNumberOfDiscards = maximumNumberOfDiscards;
    }
    
    /**
     * Copy/clone constructor; the {@link #raceColumns} collection is created as a copy and not just assigned by reference.
     * Therefore, altering the elements in the {@link #raceColumns} collection of the new object does not alter the {@link #raceColumns}
     * collection in <code>otherSeries</code>.
     */
    public SeriesDTO(SeriesDTO otherSeries) {
        this(otherSeries.getName(), otherSeries.getFleets(),
                otherSeries.getRaceColumns() == null ? null : new ArrayList<RaceColumnDTO>(otherSeries.getRaceColumns()),
                otherSeries.isMedal(), otherSeries.isFleetsCanRunInParallel(), otherSeries.getDiscardThresholds(), otherSeries.isStartsWithZeroScore(),
                otherSeries.isFirstColumnIsNonDiscardableCarryForward(), otherSeries.hasSplitFleetContiguousScoring(),
                otherSeries.getMaximumNumberOfDiscards());
    }

    public boolean hasSplitFleetContiguousScoring() {
        return hasSplitFleetContiguousScoring;
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

    public void setFleets(List<FleetDTO> fleets) {
        this.fleets = fleets;
    }

    public boolean isMedal() {
        return isMedal;
    }

    public void setMedal(boolean isMedal) {
        this.isMedal = isMedal;
    }

    public boolean isFleetsCanRunInParallel() {
        return isFleetsCanRunInParallel;
    }

    public void setFleetsCanRunInParallel(boolean isFleetsCanRunInParallel) {
        this.isFleetsCanRunInParallel = isFleetsCanRunInParallel;
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

    public Boolean isStartsWithZeroScore() {
        return startsWithZeroScore;
    }

    public void setStartsWithZeroScore(Boolean startsWithZeroScore) {
        this.startsWithZeroScore = startsWithZeroScore;
    }
    
    public void setFirstColumnIsNonDiscardableCarryForward(boolean firstColumnIsNonDiscardableCarryForward) {
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
    }

    public boolean isFirstColumnIsNonDiscardableCarryForward() {
        return firstColumnIsNonDiscardableCarryForward;
    }

    public void setSplitFleetContiguousScoring(Boolean hasSplitFleetContiguousScoring) {
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
    }

    public Integer getMaximumNumberOfDiscards() {
        return maximumNumberOfDiscards;
    }

    public void setMaximumNumberOfDiscards(Integer maximumNumberOfDiscards) {
        this.maximumNumberOfDiscards = maximumNumberOfDiscards;
    }
}
