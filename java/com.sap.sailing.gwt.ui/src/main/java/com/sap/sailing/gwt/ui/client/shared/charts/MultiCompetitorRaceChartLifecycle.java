package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentConstructorArgs;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorRaceChartLifecycle implements ComponentLifecycle<MultiCompetitorRaceChart,
    MultiCompetitorRaceChartSettings, MultiCompetitorRaceChartSettingsComponent, MultiCompetitorRaceChartLifecycle.MultiCompetitorRaceChartConstructorArgs> {
    private final StringMessages stringMessages;
    
    public MultiCompetitorRaceChartLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public MultiCompetitorRaceChartSettingsComponent getSettingsDialogComponent(MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettingsComponent(settings, stringMessages, true);
    }

    @Override
    public MultiCompetitorRaceChartSettings createDefaultSettings() {
        ChartSettings chartSettings = new ChartSettings(AbstractCompetitorRaceChart.DEFAULT_STEPSIZE);
        DetailType defaultDetailType = DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD;
        return new MultiCompetitorRaceChartSettings(chartSettings, defaultDetailType);
    }

    @Override
    public MultiCompetitorRaceChartSettings cloneSettings(MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettings(new ChartSettings(settings.getStepSize()), settings.getDetailType());
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MultiCompetitorRaceChart createComponent(MultiCompetitorRaceChartConstructorArgs ComponentConstructorArgs,
            MultiCompetitorRaceChartSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    public class MultiCompetitorRaceChartConstructorArgs implements ComponentConstructorArgs<MultiCompetitorRaceChart, MultiCompetitorRaceChartSettings> {
        public MultiCompetitorRaceChartConstructorArgs() {
        }
        
        @Override
        public MultiCompetitorRaceChart createComponent(MultiCompetitorRaceChartSettings newSettings) {
            return null;
        }
    }

}
