package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * Displays a {@code double} value that is assumed to represent seconds as a time in hours, minutes and seconds.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TotalRaceColumn extends FormattedDoubleDetailTypeColumn {

    protected TotalRaceColumn(DetailType detailType, LegDetailField<Double> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        Double timeInSeconds = getDoubleValue(object);
        if(timeInSeconds == null){
            return "N/A";
        }
        return String.valueOf(timeInSeconds.intValue());
    }
}
