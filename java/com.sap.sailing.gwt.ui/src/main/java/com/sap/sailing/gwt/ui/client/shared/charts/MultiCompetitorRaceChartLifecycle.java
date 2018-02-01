package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorRaceChartLifecycle implements ComponentLifecycle<MultiCompetitorRaceChartSettings> {
    private final StringMessages stringMessages;
    private List<DetailType> allowedDetailTypes;
    
    public static final String ID = "cc";
    
    /**
     * Lifecycle for the Chart currently used in the Raceboard to display additional data.
     * 
     * @param allowedDetailTypes
     *            Is a List of all valid DetailTypes for this chart. These are dependant on the environment, for example
     *            foiling races with Brave-Devices can show additional DetailTypes. The order of the List determines the
     *            order of the options in the related settingsdialog
     */
    public MultiCompetitorRaceChartLifecycle(StringMessages stringMessages, List<DetailType> allowedDetailTypes) {
        this.stringMessages = stringMessages;
        this.allowedDetailTypes = allowedDetailTypes;
    }

    @Override
    public MultiCompetitorRaceChartSettingsComponent getSettingsDialogComponent(MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettingsComponent(settings, stringMessages, allowedDetailTypes);
    }

    @Override
    public MultiCompetitorRaceChartSettings createDefaultSettings() {
        return new MultiCompetitorRaceChartSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MultiCompetitorRaceChartSettings extractUserSettings(MultiCompetitorRaceChartSettings settings) {
        return settings;
    }

    @Override
    public MultiCompetitorRaceChartSettings extractDocumentSettings(MultiCompetitorRaceChartSettings settings) {
        return settings;
    }

    public List<DetailType> getAllowedDetailTypes() {
        return allowedDetailTypes;
    }
}
