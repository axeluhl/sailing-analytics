package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;

public class TimeSinceLastGpsFixColumn extends FormattedDoubleDetailTypeColumn {

    public TimeSinceLastGpsFixColumn(DetailType detailType,
            com.sap.sailing.gwt.ui.leaderboard.DetailTypeColumn.LegDetailField<Double> field, String headerStyle,
            String columnStyle) {
        super(detailType, field, headerStyle, columnStyle);
    }

    @Override
    protected MinMaxRenderer createMinMaxRenderer() {
        return new TrackingQualityMinMaxRenderer(this, getComparator());
    }

}
