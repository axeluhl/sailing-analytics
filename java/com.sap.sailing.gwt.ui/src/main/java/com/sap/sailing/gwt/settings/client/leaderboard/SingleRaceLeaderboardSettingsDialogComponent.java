package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.CheckBox;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.LeaderboardSettings.RaceColumnSelectionStrategies;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.util.SettingsDefaultValuesUtils;

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
        List<String> namesOfRaceColumnsToShow = null;
        if (activeRaceColumnSelectionStrategy == RaceColumnSelectionStrategies.EXPLICIT) {
            namesOfRaceColumnsToShow = new ArrayList<String>();
            for (Map.Entry<String, CheckBox> entry : raceColumnCheckboxes.entrySet()) {
                if (entry.getValue().getValue()) {
                    namesOfRaceColumnsToShow.add(entry.getKey());
                }
            }
        }
        Long delayBetweenAutoAdvancesValue = refreshIntervalInSecondsBox.getValue();
        Integer lastNRacesToShowValue = activeRaceColumnSelectionStrategy == RaceColumnSelectionStrategies.LAST_N
                ? numberOfLastRacesToShowBox.getValue() : null;
        SingleRaceLeaderboardSettings newSettings = new SingleRaceLeaderboardSettings(maneuverDetailsToShow,
                legDetailsToShow, raceDetailsToShow, overallDetailsToShow, namesOfRaceColumnsToShow,
                /* nameOfRacesToShow */null, lastNRacesToShowValue, initialSettings.isAutoExpandPreSelectedRace(),
                1000l * (delayBetweenAutoAdvancesValue == null ? 0l : delayBetweenAutoAdvancesValue.longValue()), null,
                true, /* updateUponPlayStateChange */ true, activeRaceColumnSelectionStrategy,
                /* showAddedScores */ showAddedScoresCheckBox.getValue().booleanValue(),
                /* showOverallColumnWithNumberOfRacesSailedPerCompetitor */ showOverallColumnWithNumberOfRacesSailedPerCompetitorCheckBox
                        .getValue().booleanValue(),
                showCompetitorSailIdColumnheckBox.getValue(), showCompetitorFullNameColumnCheckBox.getValue(),
                isCompetitorNationalityColumnVisible.getValue());
        SettingsDefaultValuesUtils.keepDefaults(initialSettings, newSettings);
        return newSettings;
    }
}
