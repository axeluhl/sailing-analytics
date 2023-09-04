package com.sap.sailing.domain.common;

import java.util.Arrays;

/**
 * The reasons why a competitor may get the maximum number of points, usually equaling the
 * number of competitors enlisted for the regatta plus one.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public enum MaxPointsReason {
    
    /** The competitor finished the race properly */
    NONE(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ false),
    /** Did Not Start */
    DNS(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ true), 
    /** Did Not Finish */
    DNF(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** DiSQualified */
    DSQ(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** On Course Side (jumped the gun) */
    OCS(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ true),
    /** Disqualified, non-discardable */
    DND(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** 20 % penalty under rule 30.2 */
    ZFP(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ false),
    /** Took a Scoring penalty under rule 44.3 (a) */
    SCP(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ true, /* calculateScoreDuringRace */ true),
    /** Disqualification not excludable under rule 90.3 (b) */
    DNE(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Disqualification for gross misconduct not excludable under rule 90.3 (b) */
    DGM(/* discardable */ false, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Redress given */
    RDG(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ true, /* calculateScoreDuringRace */ true),
    /** Black Flag Disqualified */
    BFD(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ true),
    /** Did Not Compete */
    DNC(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ true),
    /** Retired After Finishing */
    RAF(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Discretionary Penalty Imposed by the race committee */
    DPI(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ true, /* calculateScoreDuringRace */ true),
    /** Retired */
    RET(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Uniform Flag Disqualification */
    UFD(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ true),
    /** Time limit Expired */
    TLE(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Standard Penalty by Race Committee; gives a certain number of penalty points on top of rank-inferred score */
    STP(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ true, /* calculateScoreDuringRace */ true),
    /** Disqualified after causing a tangle in an incident */
    DCT(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Retired after causing a tangle */
    RCT(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /** Did not sail the course */
    NSC(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ true, /* appliesAtStartOfRace */ false),
    /**
     * Scoring adjustment; originally from the German Sailing League / Bundesliga; Scores to be computed based on RRS
     * A9b: "points equal to the average, to the nearest tenth of a point (0.05 to be rounded upward), of her points in
     * all the races before the race in question"
     */
    SCA(/* discardable */ true, /* advanceCompetitorsTrackedWorse */ false, /* appliesAtStartOfRace */ true)
    ;
    
    private final boolean discardable;
    
    private final boolean advanceCompetitorsTrackedWorse;
    
    /**
     * Penalties that take effect already when the race is started; for example, a {@link #DNC} will apply
     * when the race starts, and the competitor shall immediately get the corresponding score assigned,
     * either based on the official score correction, or as calculated from the penalty code by the
     * scoring scheme if no official score correction has been provided.<p>
     * 
     * Note also {@ilnk #calculateScoreDuringRace} which can qualify the above such that although the
     * penalty takes effects beginning with the start of the race, the official score correction will
     * apply only when the competitor has finished, and up to that point the score shall be corrected
     * based on the tracked rank during the race.
     */
    private final boolean appliesAtStartOfRace;
    
    /**
     * Some penalties shall have a scoring effect already during the race but shall not be set by the
     * corrected score that applies only after the competitor has finished but shall be calculated by the
     * scoring scheme based on the type of penalty. For example, an {@link #STP} may add a penalty score
     * of 1.0 to the score based on the tracked rank which shall apply already during the race; yet, the
     * official corrected score shall be applied only after the end of the race so that during the race
     * a viewer can see the tracking-based "live" score.
     */
    private final boolean calculateScoreDuringRace;

    private MaxPointsReason(boolean discardable, boolean advanceCompetitorsTrackedWorse, boolean appliesAtStartOfRace) {
        this(discardable, advanceCompetitorsTrackedWorse, appliesAtStartOfRace, /* calculateScoreDuringRace */ false);
    }

    private MaxPointsReason(boolean discardable, boolean advanceCompetitorsTrackedWorse, boolean appliesAtStartOfRace, boolean calculateScoreDuringRace) {
        this.discardable = discardable;
        this.advanceCompetitorsTrackedWorse = advanceCompetitorsTrackedWorse;
        this.appliesAtStartOfRace = appliesAtStartOfRace;
        this.calculateScoreDuringRace = calculateScoreDuringRace;
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

    public boolean isAppliesAtStartOfRace() {
        return appliesAtStartOfRace;
    }

    public static MaxPointsReason[] getLexicographicalValues() {
        MaxPointsReason[] result = MaxPointsReason.values();
        Arrays.sort(result, (o1, o2) -> o1.name().compareToIgnoreCase(o2.name()));
        return result;
    }

    public boolean isCalculateScoreDuringRace() {
        return calculateScoreDuringRace;
    }
}
