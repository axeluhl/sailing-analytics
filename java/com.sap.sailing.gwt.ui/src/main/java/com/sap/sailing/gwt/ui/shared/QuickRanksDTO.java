package com.sap.sailing.gwt.ui.shared;

/**
 * Combines a list of {@link QuickRankDTO} objects telling about the (calculated, according to any handicap ranking metric
 * in place)) ranks of the competitors with an information about the boat farthest ahead in the race. The latter is useful,
 * e.g., to draw an advantage line on this boat leading the race without considering any handicap ranking metric.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class QuickRanksDTO {
    /**
     * Lists the competitors in the order of estimated calculated time when reaching the competitor farthest ahead.
     * This is the prediction for the ultimate calculated ranking.
     */
    private final Iterable<QuickRankDTO> quickRanksAfterCalculatedTime;
    
    public QuickRanksDTO(Iterable<QuickRankDTO> quickRanks) {
        super();
        this.quickRanksAfterCalculatedTime = quickRanks;
    }

    public Iterable<QuickRankDTO> getQuickRanks() {
        return quickRanksAfterCalculatedTime;
    }
}
