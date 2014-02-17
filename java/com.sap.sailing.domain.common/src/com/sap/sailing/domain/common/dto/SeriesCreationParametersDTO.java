package com.sap.sailing.domain.common.dto;

import java.io.Serializable;
import java.util.List;

public class SeriesCreationParametersDTO implements Serializable {
    private static final long serialVersionUID = -3205172230707607515L;

    private List<FleetDTO> fleets;

    private boolean medal;

    private boolean startsWithZero;
    
    private boolean firstColumnIsNonDiscardableCarryForward;

    private int[] discardingThresholds;

    private boolean hasSplitFleetContiguousScoring;
    
    SeriesCreationParametersDTO() {}

    public SeriesCreationParametersDTO(List<FleetDTO> fleets, boolean medal, boolean startsWithZero, boolean firstColumnIsNonDiscardableCarryForward,
            int[] discardingThresholds, boolean hasSplitFleetContiguousScoring) {
        super();
        this.fleets = fleets;
        this.medal = medal;
        this.startsWithZero = startsWithZero;
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
        this.discardingThresholds = discardingThresholds;
    }

    public List<FleetDTO> getFleets() {
        return fleets;
    }

    public boolean isMedal() {
        return medal;
    }

    public boolean isStartsWithZero() {
        return startsWithZero;
    }

    public boolean hasSplitFleetContiguousScoring() {
        return hasSplitFleetContiguousScoring;
    }

    public int[] getDiscardingThresholds() {
        return discardingThresholds;
    }

    public boolean isFirstColumnIsNonDiscardableCarryForward() {
        return firstColumnIsNonDiscardableCarryForward;
    }
    
}
