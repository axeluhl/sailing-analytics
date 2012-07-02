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
    NONE(true),
    /** Did Not Start */
    DNS(true), 
    /** Did Not Finish */
    DNF(true),
    /** DiSQualified */
    DSQ(true),
    /** On Course Side (jumped the gun) */
    OCS(true),
    /** Disqualified, non-discardable */
    DND(false),
    /** 20 % penalty under rule 30.2 */
    ZFP(true),
    /** Took a Scoring penalty under rule 44.3 (a) */
    SCP(true),
    /** Disqualification not excludable under rule 90.3 (b) */
    DNE(false),
    /** Disqualification for gross misconduct not excludable under rule 90.3 (b) */
    DGM(false),
    /** Redress given */
    RDG(true),
    /** Black Flag Disqualified */
    BFD(true),
    /** Did Not Compete */
    DNC(true),
    /** Retired After Finishing */
    RAF(true);
    
    private final boolean discardable;

    private MaxPointsReason(boolean discardable) {
        this.discardable = discardable;
    }

    public boolean isDiscardable() {
        return discardable;
    }
}