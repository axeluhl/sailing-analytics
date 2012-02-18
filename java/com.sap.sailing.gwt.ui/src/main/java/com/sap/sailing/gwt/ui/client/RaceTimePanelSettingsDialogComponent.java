package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public class RaceTimePanelSettingsDialogComponent extends TimePanelSettingsDialogComponent<RaceTimePanelSettings> {
    
    public RaceTimePanelSettingsDialogComponent(RaceTimePanelSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<RaceTimePanelSettings> dialog) {
        Widget widget = super.getAdditionalWidget(dialog);

        HorizontalPanel labelAndTDelayForLiveBoxPanel = new HorizontalPanel();
        labelAndTDelayForLiveBoxPanel.setSpacing(5);
        labelAndTDelayForLiveBoxPanel.add(new Label("Delay for live mode:"));
        RaceTimesInfoDTO raceTimesInfo = initialSettings.getRaceTimesInfo();
        if(raceTimesInfo != null && raceTimesInfo.startOfTracking != null) {
            long delayforLiveModeInMs = System.currentTimeMillis() - raceTimesInfo.getStartOfTracking().getTime();
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
