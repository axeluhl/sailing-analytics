package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;

public class PitchColumn extends FormattedDoubleDetailTypeColumn {

    public PitchColumn(DetailType detailType, LegDetailField<Double> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    protected MinMaxRenderer createMinMaxRenderer() {
        return new PitchBearingRender(this, getComparator());
    }
}
