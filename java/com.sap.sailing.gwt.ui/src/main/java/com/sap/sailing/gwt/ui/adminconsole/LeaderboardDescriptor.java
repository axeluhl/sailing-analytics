package com.sap.sailing.gwt.ui.adminconsole;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

/**
 * A descriptor class for creating and editing basic data of a leaderboard
 * @author Frank
 *
 */
public class LeaderboardDescriptor {
    public String name;
    public ScoringSchemeType scoringScheme; 
    public int[] discardThresholds;
    public RegattaDTO regatta;

    public LeaderboardDescriptor() {
    }

    public LeaderboardDescriptor(String name, ScoringSchemeType scoringScheme, int[] discardThresholds,
            RegattaDTO regatta) {
        this.name = name;
        this.scoringScheme = scoringScheme;
        this.discardThresholds = discardThresholds;
        this.regatta = regatta;
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

    public RegattaDTO getRegatta() {
        return regatta;
    }

    public void setRegatta(RegattaDTO regatta) {
        this.regatta = regatta;
    }

}
