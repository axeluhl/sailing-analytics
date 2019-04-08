package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * Shows the {@link LeaderboardRowDTO#totalTimeSailedDownwindInSeconds} attribute.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class MaxSpeedOverallColumn extends FormattedDoubleLeaderboardRowDTODetailTypeColumn {
    
    private static class MaxSpeedOverallField implements DataExtractor<Double, LeaderboardRowDTO> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.maximumSpeedOverGroundInKnots;
        }
    }

    public MaxSpeedOverallColumn(String headerStyle, String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(DetailType.OVERALL_MAXIMUM_SPEED_OVER_GROUND_IN_KNOTS, new MaxSpeedOverallField(), headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    /**
     * Adds the time point when the maximum speed was achieved as tool tip / title
     */
    @Override
    protected String getTitle(LeaderboardRowDTO row) {
        String result = null;
        if (row.whenMaximumSpeedOverGroundWasAchieved != null) {
            result = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM).format(row.whenMaximumSpeedOverGroundWasAchieved);
        }
        return result;
    }
}
