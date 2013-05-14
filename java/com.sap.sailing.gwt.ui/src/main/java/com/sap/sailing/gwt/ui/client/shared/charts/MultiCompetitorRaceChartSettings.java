package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;

public class MultiCompetitorRaceChartSettings extends ChartSettings {
    private final DetailType detailType;

    public MultiCompetitorRaceChartSettings(ChartSettings superResult, DetailType detailType) {
        super(superResult);
        this.detailType = detailType;
    }

    public DetailType getDetailType() {
        return detailType;
    }
}
