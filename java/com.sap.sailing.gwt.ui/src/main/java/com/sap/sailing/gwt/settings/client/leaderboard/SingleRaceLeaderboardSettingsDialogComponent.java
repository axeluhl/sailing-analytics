package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SingleRaceLeaderboardSettingsDialogComponent
        extends LeaderboardSettingsDialogComponent<SingleRaceLeaderboardSettings> {

    public SingleRaceLeaderboardSettingsDialogComponent(SingleRaceLeaderboardSettings initialSettings,
            List<String> allRaceColumnNames, StringMessages stringMessages) {
        super(initialSettings, allRaceColumnNames, stringMessages);
    }

    @Override
    public SingleRaceLeaderboardSettings getResult() {
        List<DetailType> maneuverDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : maneuverDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                maneuverDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> overallDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : overallDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                overallDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> raceDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : raceDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                raceDetailsToShow.add(entry.getKey());
            }
        }
        List<DetailType> legDetailsToShow = new ArrayList<DetailType>();
        for (Map.Entry<DetailType, CheckBox> entry : legDetailCheckboxes.entrySet()) {
            if (entry.getValue().getValue()) {
                legDetailsToShow.add(entry.getKey());
            }
        }
        Long delayBetweenAutoAdvancesValue = refreshIntervalInSecondsBox.getValue();
        SingleRaceLeaderboardSettings newSettings = new SingleRaceLeaderboardSettings(maneuverDetailsToShow,
                legDetailsToShow, raceDetailsToShow, overallDetailsToShow, initialSettings.isAutoExpandPreSelectedRace(),
                1000l * (delayBetweenAutoAdvancesValue == null ? 0l : delayBetweenAutoAdvancesValue.longValue()), 
                /* updateUponPlayStateChange */ true, activeRaceColumnSelectionStrategy,
                /* showAddedScores */ showAddedScoresCheckBox.getValue().booleanValue(),
                /* showOverallColumnWithNumberOfRacesSailedPerCompetitor */ showOverallColumnWithNumberOfRacesSailedPerCompetitorCheckBox
                        .getValue().booleanValue(),
                showCompetitorSailIdColumnheckBox.getValue(), showCompetitorFullNameColumnCheckBox.getValue(),
                isCompetitorNationalityColumnVisible.getValue());
        SettingsDefaultValuesUtils.keepDefaults(initialSettings, newSettings);
        return newSettings;
    }
    
    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        FlowPanel dialogPanel = new FlowPanel();
        dialogPanel.ensureDebugId("LeaderboardSettingsPanel");
        dialogPanel.add(createOverallDetailPanel(dialog));
        dialogPanel.add(createRaceDetailPanel(dialog));
        dialogPanel.add(createRaceStartAnalysisPanel(dialog));
        dialogPanel.add(createLegDetailsPanel(dialog));
        dialogPanel.add(createManeuverDetailsPanel(dialog));
        dialogPanel.add(createTimingDetailsPanel(dialog));
        return dialogPanel;
    }
}
