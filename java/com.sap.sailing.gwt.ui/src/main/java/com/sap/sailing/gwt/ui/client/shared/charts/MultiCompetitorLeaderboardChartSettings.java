package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.AbstractSettings;

public class MultiCompetitorLeaderboardChartSettings extends AbstractSettings {
    private final DetailType detailType;

    public MultiCompetitorLeaderboardChartSettings(DetailType detailType) {
        this.detailType = detailType;
    }

    public DetailType getDetailType() {
        return detailType;
    }
}
