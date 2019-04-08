package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class TimeSinceLastGpsFixColumn extends FormattedDoubleLeaderboardRowDTODetailTypeColumn {

    public TimeSinceLastGpsFixColumn(DetailType detailType,
            DataExtractor<Double, LeaderboardRowDTO> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    protected MinMaxRenderer<LeaderboardRowDTO> createMinMaxRenderer() {
        return new TrackingQualityMinMaxRenderer(this, getComparator());
    }

}
