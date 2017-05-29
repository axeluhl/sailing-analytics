package com.sap.sailing.xrr.structureimport;

public class SeriesParameters {
    private boolean firstColumnIsNonDiscardableCarryForward = false;
    private boolean hasSplitFleetContiguousScoring = false;
    private boolean startswithZeroScore = false;
    private int[] discardingThresholds = null;
    private Integer maximumNumberOfDiscards = null;

    public SeriesParameters(boolean firstColumnIsNonDiscardableCarryForward, boolean hasSplitFleetContiguousScoring,
            boolean startswithZeroScore, int[] discardingThresholds, Integer maximumNumberOfDiscards) {
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
        this.startswithZeroScore = startswithZeroScore;
        this.discardingThresholds = discardingThresholds;
        this.maximumNumberOfDiscards = maximumNumberOfDiscards;
    }

    public boolean isFirstColumnIsNonDiscardableCarryForward() {
        return firstColumnIsNonDiscardableCarryForward;
    }

    public void setFirstColumnIsNonDiscardableCarryForward(boolean firstColumnIsNonDiscardableCarryForward) {
        this.firstColumnIsNonDiscardableCarryForward = firstColumnIsNonDiscardableCarryForward;
    }

    public boolean isHasSplitFleetContiguousScoring() {
        return hasSplitFleetContiguousScoring;
    }

    public void setHasSplitFleetContiguousScoring(boolean hasSplitFleetContiguousScoring) {
        this.hasSplitFleetContiguousScoring = hasSplitFleetContiguousScoring;
    }

    public boolean isStartswithZeroScore() {
        return startswithZeroScore;
    }

    public void setStartswithZeroScore(boolean startswithZeroScore) {
        this.startswithZeroScore = startswithZeroScore;
    }

    public int[] getDiscardingThresholds() {
        return discardingThresholds;
    }

    public void setDiscardingThresholds(int[] discardingThresholds) {
        this.discardingThresholds = discardingThresholds;
    }

    public Integer getMaximumNumberOfDiscards() {
        return maximumNumberOfDiscards;
    }
}
