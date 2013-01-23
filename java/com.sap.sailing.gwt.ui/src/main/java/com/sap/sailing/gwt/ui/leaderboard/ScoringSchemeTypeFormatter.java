package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class ScoringSchemeTypeFormatter {
    public static String format(ScoringSchemeType scoringSchemeType, StringMessages stringConstants) {
        switch (scoringSchemeType) {
        case LOW_POINT:
            return stringConstants.scoringSchemeLowPointSystem();
        case HIGH_POINT:
            return stringConstants.scoringSchemeHighPointSystem();
        case HIGH_POINT_ESS_OVERALL:
            return stringConstants.scoringSchemeHighPointEssOverall();
        case HIGH_POINT_LAST_BREAKS_TIE:
            return stringConstants.scoringSchemeHighPointLastBreaksTie();
        case HIGH_POINT_FIRST_GETS_TEN:
            return stringConstants.scoringSchemeHighPointFirstGetsTen();
        }
        return null;
    }
}
