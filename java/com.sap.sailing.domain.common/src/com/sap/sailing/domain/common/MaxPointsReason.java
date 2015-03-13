package com.sap.sailing.domain.common;

/**
 * The reasons why a competitor may get the maximum number of points, usually equaling the
 * number of competitors enlisted for the regatta plus one.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum MaxPointsReason {
    
    /** The competitor finished the race properly */
    NONE(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false),
    /** Did Not Start */
    DNS(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true), 
    /** Did Not Finish */
    DNF(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** DiSQualified */
    DSQ(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** On Course Side (jumped the gun) */
    OCS(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** Disqualified, non-discardable */
    DND(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true),
    /** 20 % penalty under rule 30.2 */
    ZFP(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false),
    /** Took a Scoring penalty under rule 44.3 (a) */
    SCP(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false),
    /** Disqualification not excludable under rule 90.3 (b) */
    DNE(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true),
    /** Disqualification for gross misconduct not excludable under rule 90.3 (b) */
    DGM(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true),
    /** Redress given */
    RDG(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false),
    /** Black Flag Disqualified */
    BFD(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** Did Not Compete */
    DNC(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** Retired After Finishing */
    RAF(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true),
    /** Discretionary Penalty Imposed by the race committee */
    DPI(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false),
    /** Retired */
    RET(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true);
    UFD(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true);
    
    private final boolean discardable;
    
    private final boolean advanceCompetitorsTrackedWorse;

    private MaxPointsReason(boolean discardable, boolean advanceCompetitorsTrackedWorse) {
        this.discardable = discardable;
        this.advanceCompetitorsTrackedWorse = advanceCompetitorsTrackedWorse;
    }

    public boolean isDiscardable() {
        return discardable;
    }
    
    /**
     * Most "max points reasons" are a penalty. If the competitor has finished the race and by the penalty is ranked to the "bottom,"
     * those competitors tracked worse usually will advance by one rank. However, "max points reasons" such as a redress given (RDG)
     * are different. In this case, only the points awarded to the competitor change without any effect on other competitors.
     */
    public boolean isAdvanceCompetitorsTrackedWorse() {
        return advanceCompetitorsTrackedWorse;
    }
}
