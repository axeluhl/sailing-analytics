package com.sap.sailing.server.gateway.deserialization.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.impl.BoatClassImpl;
import com.sap.sailing.domain.base.impl.CourseAreaImpl;
import com.sap.sailing.domain.base.impl.RegattaImpl;
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.leaderboard.ScoringScheme;
import com.sap.sailing.domain.leaderboard.impl.HighPoint;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall;
import com.sap.sailing.domain.leaderboard.impl.HighPointExtremeSailingSeriesOverall12PointsMax;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets10Or8AndLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets12Or8AndLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointFirstGets1LastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointLastBreaksTie;
import com.sap.sailing.domain.leaderboard.impl.HighPointMatchRacing;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsEight;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsEightAndInterpolation;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsFive;
import com.sap.sailing.domain.leaderboard.impl.HighPointWinnerGetsSix;
import com.sap.sailing.domain.leaderboard.impl.LowPoint;
import com.sap.sailing.domain.leaderboard.impl.LowPointForLeagueOverallLeaderboard;
import com.sap.sailing.domain.leaderboard.impl.LowPointTieBreakBasedOnLastSeriesOnly;
import com.sap.sailing.domain.leaderboard.impl.LowPointWinnerGetsZero;
import com.sap.sailing.domain.leaderboard.impl.LowPointWithEliminationsAndRoundsWinnerGets07;
import com.sap.sailing.domain.ranking.OneDesignRankingMetric;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RegattaJsonDeserializer implements JsonDeserializer<Regatta> {
    public static final String COURSE_AREA = "Course Area";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_BOATCLASS = "boatclass";
    public static final String FIELD_START_DATE = "startDate";
    public static final String FIELD_END_DATE = "endDate";
    public static final String FIELD_SCORINGSYSTEM = "scoringSystem";
    public static final String FIELD_SERIES = "series";
    public static final String FIELD_COMPETITORS = "competitors";
    public static final String FIELD_TRACKED_RACES = "trackedRaces";
    public static final String FIELD_COURSE_AREA_ID = "courseAreaId";
    public static final String FIELD_ID = "id";

    public RegattaJsonDeserializer() {
    }

    @Override
    public Regatta deserialize(JSONObject object) throws JsonDeserializationException {
        String name = (String) object.get(FIELD_NAME);
        BoatClass boatClass = null;
        if (object.get(FIELD_BOATCLASS) != null) {
            boatClass = new BoatClassImpl((String) object.get(FIELD_BOATCLASS), /* typicallyStartsUpwind */ false);
        }

        TimePoint startTime = null;
        if (object.get(FIELD_START_DATE) != null) {
            startTime = new MillisecondsTimePoint((long) object.get(FIELD_START_DATE));
        }

        TimePoint endTime = null;
        if (object.get(FIELD_END_DATE) != null) {
            endTime = new MillisecondsTimePoint((long) object.get(FIELD_END_DATE));
        }
        ScoringSchemeType schemeType = ScoringSchemeType.valueOf((String) object.get(FIELD_SCORINGSYSTEM));
        ScoringScheme scoringScheme = createScoringScheme(schemeType);
        Serializable id = (Serializable) object.get(FIELD_ID);

        System.out.println(object.get(FIELD_COURSE_AREA_ID));
        CourseArea courseArea = null;
        if (object.get(FIELD_COURSE_AREA_ID) != null) {
            courseArea = new CourseAreaImpl(COURSE_AREA, UUID.fromString((String) object.get(FIELD_COURSE_AREA_ID)));
        }
        return new RegattaImpl(name, boatClass, startTime, endTime, Collections.emptyList(), /* persistent */ false,
                scoringScheme, id, courseArea, OneDesignRankingMetric::new);
    }

    public ScoringScheme createScoringScheme(ScoringSchemeType scoringSchemeType) {
        switch (scoringSchemeType) {
        case LOW_POINT:
            return new LowPoint();
        case HIGH_POINT:
            return new HighPoint();
        case HIGH_POINT_ESS_OVERALL:
            return new HighPointExtremeSailingSeriesOverall();
        case HIGH_POINT_ESS_OVERALL_12:
            return new HighPointExtremeSailingSeriesOverall12PointsMax();
        case HIGH_POINT_LAST_BREAKS_TIE:
            return new HighPointLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_ONE:
            return new HighPointFirstGets1LastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TEN:
            return new HighPointFirstGets10LastBreaksTie();
        case LOW_POINT_WINNER_GETS_ZERO:
            return new LowPointWinnerGetsZero();
        case HIGH_POINT_WINNER_GETS_FIVE:
            return new HighPointWinnerGetsFive();
        case HIGH_POINT_WINNER_GETS_SIX:
            return new HighPointWinnerGetsSix();
        case HIGH_POINT_WINNER_GETS_EIGHT:
            return new HighPointWinnerGetsEight();
        case HIGH_POINT_MATCH_RACING:
            return new HighPointMatchRacing();
        case HIGH_POINT_WINNER_GETS_EIGHT_AND_INTERPOLATION:
            return new HighPointWinnerGetsEightAndInterpolation();
        case HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT:
            return new HighPointFirstGets10Or8AndLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TWELVE_OR_EIGHT:
            return new HighPointFirstGets12Or8AndLastBreaksTie();
        case LOW_POINT_WITH_ELIMINATIONS_AND_ROUNDS_WINNER_GETS_07:
            return new LowPointWithEliminationsAndRoundsWinnerGets07();
        case LOW_POINT_LEAGUE_OVERALL:
            return new LowPointForLeagueOverallLeaderboard();
        case LOW_POINT_TIE_BREAK_BASED_ON_LAST_SERIES_ONLY:
            return new LowPointTieBreakBasedOnLastSeriesOnly();
        }
        throw new RuntimeException("Unknown scoring scheme type " + scoringSchemeType.name());
    }
}
