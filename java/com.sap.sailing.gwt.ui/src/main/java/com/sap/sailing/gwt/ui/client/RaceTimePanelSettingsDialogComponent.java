package com.sap.sailing.gwt.ui.client;


public class RaceTimePanelSettingsDialogComponent extends TimePanelSettingsDialogComponent<RaceTimePanelSettings> {
    
    public RaceTimePanelSettingsDialogComponent(RaceTimePanelSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }

    @Override
    public RaceTimePanelSettings getResult() {
        long refreshInternal = refreshIntervalBox.getValue() == null ? -1 : (long) (refreshIntervalBox.getValue() * 1000);
        RaceTimePanelSettings result = new RaceTimePanelSettings(refreshInternal);
        return result;
    }
}
