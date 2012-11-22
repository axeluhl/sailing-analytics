package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.ScoringSchemeType;

/**
 * A descriptor class for creating and editing basic data of a leaderboard
 * @author Frank
 *
 */
public class LeaderboardDescriptor {
    private String name;
    private ScoringSchemeType scoringScheme; 
    private int[] discardThresholds;
    private String regattaName;

    public LeaderboardDescriptor() {
    }

    public LeaderboardDescriptor(String name, ScoringSchemeType scoringScheme, int[] discardThresholds, String regattaName) {
        this.name = name;
        this.scoringScheme = scoringScheme;
        this.discardThresholds = discardThresholds;
        this.regattaName = regattaName;
    }

    public LeaderboardDescriptor(String name, ScoringSchemeType scoringScheme, int[] discardThresholds) {
        this.name = name;
        this.scoringScheme = scoringScheme;
        this.discardThresholds = discardThresholds;
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
}
