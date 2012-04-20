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
    NONE,
    /** Did Not Start */
    DNS, 
    /** Did Not Finish */
    DNF,
    /** DiSQualified */
    DSQ,
    /** On Course Side (jumped the gun) */
    OCS,
    /** Disqualified, non-discardable */
    DND,
    /** Black Flag Disqualified */
    BFD,
    /** Did Not Compete */
    DNC,
    /** Retired After Finishing */
    RAF
}