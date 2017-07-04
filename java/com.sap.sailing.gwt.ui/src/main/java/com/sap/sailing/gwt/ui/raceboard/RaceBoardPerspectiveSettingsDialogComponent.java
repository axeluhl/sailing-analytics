package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class RaceBoardPerspectiveSettingsDialogComponent implements SettingsDialogComponent<RaceBoardPerspectiveOwnSettings> {
    private CheckBox showLeaderboardCheckBox; 
    private CheckBox showWindChartCheckBox; 
    private CheckBox showCompetitorsChartCheckBox;

    private final StringMessages stringMessages;
    private final RaceBoardPerspectiveOwnSettings initialSettings;
    
    public RaceBoardPerspectiveSettingsDialogComponent(RaceBoardPerspectiveOwnSettings settings, StringMessages stringMessages) {
        this.initialSettings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        showLeaderboardCheckBox = dialog.createCheckbox(stringMessages.showLeaderboard());
        showLeaderboardCheckBox.setValue(initialSettings.isShowLeaderboard());
        vp.add(showLeaderboardCheckBox);

        showWindChartCheckBox = dialog.createCheckbox(stringMessages.showWindChart());
        showWindChartCheckBox.setValue(initialSettings.isShowWindChart());
        vp.add(showWindChartCheckBox);        

        showCompetitorsChartCheckBox = dialog.createCheckbox(stringMessages.showCompetitorCharts());
        showCompetitorsChartCheckBox.setValue(initialSettings.isShowCompetitorsChart());
        vp.add(showCompetitorsChartCheckBox);        
        
        return vp;
    }
    
    @Override
    public RaceBoardPerspectiveOwnSettings getResult() {
        RaceBoardPerspectiveOwnSettings result = new RaceBoardPerspectiveOwnSettings(initialSettings.getActiveCompetitorsFilterSetName(), 
                showLeaderboardCheckBox.getValue(), showWindChartCheckBox.getValue(), showCompetitorsChartCheckBox.getValue(),
                initialSettings.isCanReplayDuringLiveRaces(), initialSettings.getInitialDurationAfterRaceStartInReplay());
        return result;
    }
    
    @Override
    public Validator<RaceBoardPerspectiveOwnSettings> getValidator() {
        return new Validator<RaceBoardPerspectiveOwnSettings>() {
            @Override
            public String getErrorMessage(RaceBoardPerspectiveOwnSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return showLeaderboardCheckBox;
    }
}
