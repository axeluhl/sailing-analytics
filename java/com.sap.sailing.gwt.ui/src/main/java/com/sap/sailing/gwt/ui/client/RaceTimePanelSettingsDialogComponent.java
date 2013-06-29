package com.sap.sailing.gwt.ui.client;


public class RaceTimePanelSettingsDialogComponent extends TimePanelSettingsDialogComponent<RaceTimePanelSettings> {
    
    public RaceTimePanelSettingsDialogComponent(RaceTimePanelSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }

    @Override
    public RaceTimePanelSettings getResult() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setRefreshInterval(refreshIntervalBox.getValue() == null ? -1 : (long) (refreshIntervalBox.getValue() * 1000));
        return result;
    }
}
