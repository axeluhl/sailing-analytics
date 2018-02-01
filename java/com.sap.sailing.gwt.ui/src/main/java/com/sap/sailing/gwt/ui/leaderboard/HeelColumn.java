package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;

public class HeelColumn extends FormattedDoubleDetailTypeColumn {

    public HeelColumn(DetailType detailType, LegDetailField<Double> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    protected MinMaxRenderer createMinMaxRenderer() {
        return new HeelBearingRender(this, getComparator());
    }

}
