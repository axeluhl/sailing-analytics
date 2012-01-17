package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.server.api.DetailType;

public class MultiChartSettings extends ChartSettings {
    private final DetailType dataToShow;
    
    public MultiChartSettings(ChartSettings superResult, DetailType dataToShow) {
        super(superResult);
        this.dataToShow = dataToShow;
    }

    public DetailType getDataToShow() {
        return dataToShow;
    }
}
