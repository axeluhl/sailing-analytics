package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;

public class MultiCompetitorRaceChartSettings extends ChartSettings {
    private final DetailType firstDetailType;
    private final DetailType secondDetailType;

    public MultiCompetitorRaceChartSettings(ChartSettings settings, DetailType firstDetailType,
            DetailType secondDetailType) {
        super(settings);
        this.firstDetailType = firstDetailType;
        this.secondDetailType = secondDetailType;
    }

    public DetailType getFirstDetailType() {
        return firstDetailType;
    }

    public DetailType getSecondDetailType() {
        return secondDetailType;
    }
}
