package com.sap.sailing.domain.masterdataimport;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.leaderboard.impl.ScoreCorrectionImpl;

public class FlexibleLeaderboardMasterData extends LeaderboardMasterData {

    private ScoringScheme scoringScheme;

    private String courseAreaId;

    private CourseArea courseArea;

    private List<Pair<String, Boolean>> raceColumns;

    public FlexibleLeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<String, Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection,
            ScoringScheme scoringScheme, String courseAreaId, List<Pair<String, Boolean>> raceColumns,
            Map<String, Double> carriedPoints, List<String> suppressedCompetitors,
            Map<String, String> displayNamesByCompetitorId) {
        super(name, displayName, resultDiscardingRule, competitorsById, scoreCorrection, carriedPoints,
                suppressedCompetitors, displayNamesByCompetitorId);
        this.scoringScheme = scoringScheme;
        this.courseAreaId = courseAreaId;
        this.raceColumns = raceColumns;
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
        return new FlexibleLeaderboardImpl(getName(), new ScoreCorrectionImpl(), getResultDiscardingRule(),
                scoringScheme, courseArea);
    }

    public List<Pair<String, Boolean>> getRaceColumns() {
        return raceColumns;
    }

}
