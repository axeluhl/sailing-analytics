package com.sap.sailing.domain.abstractlog.race.scoring;

public enum AdditionalScoringInformationType {
    /**
     * Indicates that for a scoring scheme adhering to a max point rule
     * where the winner gets a higher number than its successors the
     * maximum number of points is lowered. The concrete amount is
     * decided by the scoring scheme.
     */
    MAX_POINTS_DECREASE_MAX_SCORE,
    
    /**
     * Unknown information scoring type - should not be used except
     * for tests
     */
    UNKNOWN
}
