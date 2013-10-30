package com.sap.sailing.domain.masterdataimport;

import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.LeaderboardMasterData;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.FlexibleLeaderboardImpl;
import com.sap.sailing.domain.racelog.RaceLogEvent;

public class FlexibleLeaderboardMasterData extends LeaderboardMasterData {

    private ScoringScheme scoringScheme;

    private String courseAreaId;

    private CourseArea courseArea;

    private List<RaceColumnMasterData> raceColumns;

    public FlexibleLeaderboardMasterData(String name, String displayName, int[] resultDiscardingRule,
            Map<String, Competitor> competitorsById, ScoreCorrectionMasterData scoreCorrection,
            ScoringScheme scoringScheme, String courseAreaId, List<RaceColumnMasterData> raceColumns,
            Map<String, Double> carriedPoints, List<String> suppressedCompetitors,
            Map<String, String> displayNamesByCompetitorId, Map<String, Map<String, List<RaceLogEvent>>> raceLogEvents) {
        super(name, displayName, resultDiscardingRule, competitorsById, scoreCorrection, carriedPoints,
                suppressedCompetitors, displayNamesByCompetitorId, raceLogEvents);
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
        return new FlexibleLeaderboardImpl(getName(), getResultDiscardingRule(), scoringScheme,
                courseArea);
    }

    public List<RaceColumnMasterData> getRaceColumns() {
        return raceColumns;
    }

}
