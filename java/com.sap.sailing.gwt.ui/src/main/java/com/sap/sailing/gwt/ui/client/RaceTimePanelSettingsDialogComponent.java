package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimePanelSettingsDialogComponent extends TimePanelSettingsDialogComponent<RaceTimePanelSettings> {
    
    public RaceTimePanelSettingsDialogComponent(RaceTimePanelSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        Widget widget = super.getAdditionalWidget(dialog);
        FlowPanel labelAndTDelayForLiveBoxPanel = new FlowPanel();
        Label delayForLiveMode = new Label(getStringMessages().delayForLiveMode());
        labelAndTDelayForLiveBoxPanel.add(delayForLiveMode);
        RaceTimesInfoDTO raceTimesInfo = initialSettings.getRaceTimesInfo();
        if (raceTimesInfo != null && raceTimesInfo.startOfRace != null) {
            long delayforLiveModeInMs = System.currentTimeMillis() - raceTimesInfo.getStartOfRace().getTime();
            labelAndTDelayForLiveBoxPanel.add(new Label(delayforLiveModeInMs / 1000 + " s"));
        }
        mainContentPanel.add(labelAndTDelayForLiveBoxPanel);
        return widget;
    }
    
    @Override
    public RaceTimePanelSettings getResult() {
        RaceTimePanelSettings result = new RaceTimePanelSettings();
        result.setDelayToLivePlayInSeconds(timeDelayBox.getValue() == null ? -1 : timeDelayBox.getValue());
        result.setRefreshInterval(refreshIntervalBox.getValue() == null ? -1 : (long) (refreshIntervalBox.getValue() * 1000));
        return result;
    }
}
