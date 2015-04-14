package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.settings.Settings;

public class MultiCompetitorLeaderboardChartSettings implements Settings {
    private final DetailType detailType;

    public MultiCompetitorLeaderboardChartSettings(DetailType detailType) {
        this.detailType = detailType;
    }

    public DetailType getDetailType() {
        return detailType;
    }
}
