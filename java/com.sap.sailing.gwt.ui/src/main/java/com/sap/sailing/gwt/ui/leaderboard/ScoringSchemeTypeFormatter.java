package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ScoringSchemeTypeFormatter {
    public static String format(ScoringSchemeType scoringSchemeType, StringMessages stringMessages) {
        switch (scoringSchemeType) {
        case LOW_POINT:
            return stringMessages.scoringSchemeLowPointSystem();
        case HIGH_POINT:
            return stringMessages.scoringSchemeHighPointSystem();
        case HIGH_POINT_ESS_OVERALL:
            return stringMessages.scoringSchemeHighPointEssOverall();
        case HIGH_POINT_LAST_BREAKS_TIE:
            return stringMessages.scoringSchemeHighPointLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_ONE:
            return stringMessages.scoringSchemeHighPointFirstGetsOne();
        case HIGH_POINT_FIRST_GETS_TEN:
            return stringMessages.scoringSchemeHighPointFirstGetsTen();
        case LOW_POINT_WINNER_GETS_ZERO:
            return stringMessages.scoringSchemeLowPointWinnerGetsZero();
        case HIGH_POINT_WINNER_GETS_FIVE:
            return stringMessages.scoringSchemeWinnerGetsFive();
        case HIGH_POINT_WINNER_GETS_SIX:
            return stringMessages.scoringSchemeWinnerGetsSix();
        case HIGH_POINT_WINNER_GETS_EIGHT:
            return stringMessages.scoringSchemeWinnerGetsEight();
        case HIGH_POINT_WINNER_GETS_EIGHT_AND_INTERPOLATION:
            return stringMessages.scoringSchemeWinnerGetsEightAndInterpolation();
        case HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT:
            return stringMessages.scoringSchemeHighPointFirstGetsTenOrEight();
        }
        return null;
    }
    
    public static String getDescription(ScoringSchemeType scoringSchemeType, StringMessages stringMessages) {
        switch(scoringSchemeType) {
        case HIGH_POINT_FIRST_GETS_TEN_OR_EIGHT:
            return stringMessages.scoringSchemeHighPointFirstGetsTenOrEightDescription();
        default:
            return format(scoringSchemeType, stringMessages);
        }
    }
}
