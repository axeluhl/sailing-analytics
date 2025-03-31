package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class RideHeightColumn extends FormattedDoubleLeaderboardRowDTODetailTypeColumn {

    public RideHeightColumn(DetailType detailType, DataExtractor<Double, LeaderboardRowDTO> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }
    
    @Override
    protected MinMaxRenderer<LeaderboardRowDTO> createMinMaxRenderer() {
        return new RideHeightQualityMinMaxRender(this, getComparator());
    }

}
