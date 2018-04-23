package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class HeelColumn extends FormattedDoubleLeaderboardRowDTODetailTypeColumn {

    public HeelColumn(DetailType detailType, DataExtractor<Double, LeaderboardRowDTO> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    protected MinMaxRenderer<LeaderboardRowDTO> createMinMaxRenderer() {
        return new HeelBearingRender(this, getComparator());
    }

}
