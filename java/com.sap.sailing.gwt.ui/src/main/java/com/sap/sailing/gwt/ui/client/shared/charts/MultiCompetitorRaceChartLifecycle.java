package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorRaceChartLifecycle implements ComponentLifecycle<MultiCompetitorRaceChart, MultiCompetitorRaceChartSettings, MultiCompetitorRaceChartSettingsComponent> {
    private final StringMessages stringMessages;
    private MultiCompetitorRaceChart component;
    
    public MultiCompetitorRaceChartLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.component = null;
    }

    @Override
    public MultiCompetitorRaceChartSettingsComponent getSettingsDialogComponent(
            MultiCompetitorRaceChartSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultiCompetitorRaceChart getComponent() {
        // TODO Auto-generated method stub
        return component;
    }

    @Override
    public MultiCompetitorRaceChartSettings createDefaultSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MultiCompetitorRaceChartSettings cloneSettings(MultiCompetitorRaceChartSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

}
