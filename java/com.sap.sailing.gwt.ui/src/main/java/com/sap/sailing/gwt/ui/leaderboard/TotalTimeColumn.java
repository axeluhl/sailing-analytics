package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.DurationAsHoursMinutesSecondsFormatter;

/**
 * Displays a {@code double} value that is assumed to represent seconds as a time in hours, minutes and seconds.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class TotalTimeColumn extends FormattedDoubleLeaderboardRowDTODetailTypeColumn {

    protected TotalTimeColumn(DetailType detailType, DataExtractor<Double, LeaderboardRowDTO> fieldWithTimeInSeconds, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, fieldWithTimeInSeconds, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        Double timeInSeconds = getDoubleValue(object);
        return new DurationAsHoursMinutesSecondsFormatter().getHoursMinutesSeconds(timeInSeconds);
    }
}
