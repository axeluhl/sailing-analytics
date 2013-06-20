package com.sap.sailing.domain.base.impl;

import java.util.Set;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;

public class FlexibleLeaderboardMasterData extends LeaderboardMasterData {
    
    private ScoringScheme scoringScheme;
    
    private String courseAreaId;
    
    private CourseArea courseArea;

    public FlexibleLeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Set<Competitor> competitors, ScoringScheme scoringScheme, String courseAreaId) {
        super(name, displayName, resultDiscardingRule, competitors);
        this.scoringScheme = scoringScheme;
        this.courseAreaId = courseAreaId;
    }

    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

    public String getCourseAreaId() {
        return courseAreaId;
    }
    
    

    public void setCourseArea(CourseArea courseArea) {
        this.courseArea = courseArea;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return new FlexibleLeaderboardImpl(getName(), new ScoreCorrectionImpl(), getResultDiscardingRule(), scoringScheme, courseArea);
    }

}
