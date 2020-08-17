package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

public class EditableLeaderboardSettings extends MultiRaceLeaderboardSettings {

    private static final long serialVersionUID = -2108718366296960650L;
    private static final String SHOW_CARRY_COLUMN_KEY = "showCarryColumn";

    protected BooleanSetting showCarryColumn;

    public EditableLeaderboardSettings(boolean showCarryColumn) {
        super();
        this.showCarryColumn.setValue(showCarryColumn);
    }
    
    public EditableLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, List<String> namesOfRaceColumnsToShow,
            Integer numberOfLastRacesToShow, Long delayBetweenAutoAdvancesInMilliseconds,
            RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy, boolean showAddedScores,
            boolean showCompetitorShortNameColumn, boolean showCompetitorFullNameColumn,
            boolean showCompetitorBoatInfoColumn, boolean isCompetitorNationalityColumnVisible,
            boolean showCarryColumn) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow,
                namesOfRaceColumnsToShow, numberOfLastRacesToShow, delayBetweenAutoAdvancesInMilliseconds,
                activeRaceColumnSelectionStrategy, showAddedScores, showCompetitorShortNameColumn,
                showCompetitorFullNameColumn, showCompetitorBoatInfoColumn, isCompetitorNationalityColumnVisible);
        this.showCarryColumn.setValue(showCarryColumn);
    }

    @Override
    protected void addChildSettings() {
        super.addChildSettings();
        showCarryColumn = new BooleanSetting(SHOW_CARRY_COLUMN_KEY,  this, true);
    }
    
    public Boolean getShowCarryColumn() {
        return showCarryColumn.getValue();
    }

    @Override
    public EditableLeaderboardSettings withNamesOfRaceColumnsToShowDefaults(
            Iterable<String> namesOfRaceColumnsToShow) {
        return withNamesOfRaceColumnsToShowDefaults(namesOfRaceColumnsToShow, true);
    }
    
    /**
     * Constructs a new {@link MultiRaceLeaderboardSettings} instance with all defaults and values taken from this
     * instance except the defaults for namesOfRaceColumnsToShow which are set to the given values.
     */
    public EditableLeaderboardSettings withNamesOfRaceColumnsToShowDefaults(
            final Iterable<String> namesOfRaceColumnsToShow, boolean showCarryDefault) {
        final EditableLeaderboardSettings newSettings = new EditableLeaderboardSettings(showCarryDefault);
        SettingsUtil.copyValuesAndDefaults(this, this, newSettings);
        newSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumnsToShow);
        return newSettings;
    }
    
}
