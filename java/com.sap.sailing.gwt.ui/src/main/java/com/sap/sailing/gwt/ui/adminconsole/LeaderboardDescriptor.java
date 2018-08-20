package com.sap.sailing.gwt.ui.adminconsole;

import java.util.UUID;

import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * A descriptor class for creating and editing basic data of a leaderboard.
 * 
 * @author Frank
 * 
 */
public class LeaderboardDescriptor {
    private String name;
    private String displayName;
    private ScoringSchemeType scoringScheme; 
    private int[] discardThresholds;
    private String regattaName;
    private UUID courseAreaId;

    public LeaderboardDescriptor() {
    }

    public LeaderboardDescriptor(String name, String displayName, ScoringSchemeType scoringScheme, int[] discardThresholds, String regattaName, UUID courseAreaId) {
        this.name = name;
        this.displayName = displayName;
        this.scoringScheme = scoringScheme;
        this.discardThresholds = discardThresholds;
        this.regattaName = regattaName;
        this.courseAreaId = courseAreaId;
    }

    /**
     * Leaves the {@link #regattaName} <code>null</code>, representing a flexible leaderboard, not a regatta leaderboard
     */
    public LeaderboardDescriptor(String name, String displayName, ScoringSchemeType scoringScheme, int[] discardThresholds, UUID courseAreaId) {
        this.name = name;
        this.displayName = displayName;
        this.scoringScheme = scoringScheme;
        this.discardThresholds = discardThresholds;
        this.courseAreaId = courseAreaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScoringSchemeType getScoringScheme() {
        return scoringScheme;
    }

    public void setScoringScheme(ScoringSchemeType scoringScheme) {
        this.scoringScheme = scoringScheme;
    }

    /**
     * @return <code>null</code>, in case the leaderboard does not explicitly define result discarding thresholds itself
     *         (e.g., a regatta leaderboard that obtains its result discarding rules from the underlying regatta's
     *         series definitions); a valid but possibly empty array otherwise
     */
    public int[] getDiscardThresholds() {
        return discardThresholds;
    }

    public void setDiscardThresholds(int[] discardThresholds) {
        this.discardThresholds = discardThresholds;
    }

    public String getRegattaName() {
        return regattaName;
    }

    public void setRegattaName(String regattaName) {
        this.regattaName = regattaName;
    }
    
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getCourseAreaId() {
        return courseAreaId;
    }

    public void setCourseAreaId(UUID courseAreaId) {
        this.courseAreaId = courseAreaId;
    }
}
