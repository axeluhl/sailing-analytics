package com.sap.sailing.gwt.ui.raceboard;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class RaceBoardPerspectiveSettingsDialogComponent implements SettingsDialogComponent<RaceBoardPerspectiveSettings> {
    private CheckBox showLeaderboardCheckBox; 
    private CheckBox showWindChartCheckBox; 
    private CheckBox showCompetitorsChartCheckBox;
    private CheckBox autoSelectMediaCheckBox;

    private final StringMessages stringMessages;
    private final RaceBoardPerspectiveSettings initialSettings;
    
    public RaceBoardPerspectiveSettingsDialogComponent(RaceBoardPerspectiveSettings settings, StringMessages stringMessages) {
        this.initialSettings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        showLeaderboardCheckBox = dialog.createCheckbox(stringMessages.show() + " " + stringMessages.leaderboard());
        showLeaderboardCheckBox.setValue(initialSettings.isShowLeaderboard());
        vp.add(showLeaderboardCheckBox);

        showWindChartCheckBox = dialog.createCheckbox(stringMessages.show() + " " + stringMessages.windChart());
        showWindChartCheckBox.setValue(initialSettings.isShowWindChart());
        vp.add(showWindChartCheckBox);        

        showCompetitorsChartCheckBox = dialog.createCheckbox(stringMessages.show() + " " + stringMessages.competitorCharts());
        showCompetitorsChartCheckBox.setValue(initialSettings.isShowCompetitorsChart());
        vp.add(showCompetitorsChartCheckBox);        
        
        autoSelectMediaCheckBox = dialog.createCheckbox("Auto select media");
        autoSelectMediaCheckBox.setValue(initialSettings.isAutoSelectMedia());
        vp.add(autoSelectMediaCheckBox);        

        return vp;
    }
    

    @Override
    public RaceBoardPerspectiveSettings getResult() {
        RaceBoardPerspectiveSettings result = new RaceBoardPerspectiveSettings(/*activeCompetitorsFilterSetName*/ null, 
                showLeaderboardCheckBox.getValue(), showWindChartCheckBox.getValue(), showCompetitorsChartCheckBox.getValue(),
                /*showViewStreamlets */ false, /*showViewStreamletColors*/ false, /*showViewSimulation*/ false,
                /*canReplayDuringLiveRaces*/ false, autoSelectMediaCheckBox.getValue(), /*defaultMedia*/ null);
        return result;
    }
    
    @Override
    public Validator<RaceBoardPerspectiveSettings> getValidator() {
        return new Validator<RaceBoardPerspectiveSettings>() {
            @Override
            public String getErrorMessage(RaceBoardPerspectiveSettings valueToValidate) {
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
