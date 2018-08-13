package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.StringSetSetting;
import com.sap.sse.common.settings.generic.support.SettingsUtil;

public class MultiRaceLeaderboardSettings extends LeaderboardSettings {
    private static final long serialVersionUID = -3445146715292390755L;

    /**
     * Only one of {@link #namesOfRaceColumnsToShow} and {@link #namesOfRacesToShow} must be non-<code>null</code>.
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to EXPLIZIT
     */
    protected StringSetSetting namesOfRaceColumnsToShow;

    /**
     * Only valid when the {@link #activeRaceColumnSelectionStrategy} is set to LAST_N
     */
    protected IntegerSetting numberOfLastRacesToShow;
    
    protected EnumSetting<RaceColumnSelectionStrategies> activeRaceColumnSelectionStrategy;

    
    public MultiRaceLeaderboardSettings() {
        super(false);
    }

    public MultiRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, List<String> namesOfRaceColumnsToShow,
            Integer numberOfLastRacesToShow,
            Long delayBetweenAutoAdvancesInMilliseconds,
            RaceColumnSelectionStrategies activeRaceColumnSelectionStrategy,
            boolean showAddedScores, boolean showCompetitorShortNameColumn, boolean showCompetitorFullNameColumn,
            boolean showCompetitorBoatInfoColumn, boolean isCompetitorNationalityColumnVisible) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow, delayBetweenAutoAdvancesInMilliseconds,
                showAddedScores, showCompetitorShortNameColumn,
                showCompetitorFullNameColumn, showCompetitorBoatInfoColumn, isCompetitorNationalityColumnVisible);
        this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        this.numberOfLastRacesToShow.setValue(numberOfLastRacesToShow);
        this.activeRaceColumnSelectionStrategy.setValue(activeRaceColumnSelectionStrategy);
    }

    public MultiRaceLeaderboardSettings(Iterable<String> namesOfRaceColumnsToShow) {
        super(false);
       this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
    }
    
    public MultiRaceLeaderboardSettings overrideDefaultsForNamesOfRaceColumns(List<String> namesOfRaceColumns) {
        final MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        SettingsUtil.copyValuesAndDefaults(this, this, newSettings);
        newSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumns);
        return newSettings;
    }
    
    /**
     * Constructs a new {@link MultiRaceLeaderboardSettings} instance with all defaults and values taken from this
     * instance except the namesOfRaceColumnsToShow. The defaults as well as the value of namesOfRaceColumnsToShow is
     * set to the given values.
     */
    public MultiRaceLeaderboardSettings withDefaultNamesOfRaceColumnsToShow(Iterable<String> namesOfRaceColumnsToShow) {
        MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        SettingsUtil.copyValuesAndDefaults(this, this, newSettings);
        newSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumnsToShow);
        newSettings.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
        return newSettings;
    }
    
    @Override
    protected void addChildSettings() {
        super.addChildSettings();
        namesOfRaceColumnsToShow = new StringSetSetting("namesOfRaceColumnsToShow", this);
        numberOfLastRacesToShow = new IntegerSetting("numberOfLastRacesToShow", this, null);
        activeRaceColumnSelectionStrategy = new EnumSetting<>("activeRaceColumnSelectionStrategy", this, RaceColumnSelectionStrategies.EXPLICIT, RaceColumnSelectionStrategies::valueOf);
    }
    
    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}. Otherwise a
     * live collection that reflects the current state of the settings of a leaderboard panel
     */
    public List<String> getNamesOfRaceColumnsToShow() {
        return activeRaceColumnSelectionStrategy.getValue() == RaceColumnSelectionStrategies.EXPLICIT ? (namesOfRaceColumnsToShow.isValuesEmpty() ? null : Util.createList(namesOfRaceColumnsToShow.getValues())) : null;
    }

    /**
     * If <code>null</code>, this is to mean that the race columns should not be modified by
     * {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}.
     */
    public Integer getNumberOfLastRacesToShow() {
        return activeRaceColumnSelectionStrategy.getValue() == RaceColumnSelectionStrategies.LAST_N ? numberOfLastRacesToShow.getValue() : null;
    }

    public RaceColumnSelectionStrategies getActiveRaceColumnSelectionStrategy() {
        return activeRaceColumnSelectionStrategy.getValue();
    }

    public MultiRaceLeaderboardSettings withRaceColumnSelectionFrom(
            MultiRaceLeaderboardSettings settingsWithRaceColunSelection) {
        MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        SettingsUtil.copyValuesAndDefaults(this, this, newSettings);
        newSettings.activeRaceColumnSelectionStrategy
                .setValue(settingsWithRaceColunSelection.getActiveRaceColumnSelectionStrategy());
        newSettings.numberOfLastRacesToShow.setValue(settingsWithRaceColunSelection.getNumberOfLastRacesToShow());
        newSettings.namesOfRaceColumnsToShow.setValues(settingsWithRaceColunSelection.getNamesOfRaceColumnsToShow());
        return newSettings;
    }

    public static MultiRaceLeaderboardSettings createDefaultSettingsWithLastNMode(int numberOfLastRacesToShow) {
        MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        newSettings.namesOfRaceColumnsToShow.setValues(null);
        newSettings.numberOfLastRacesToShow.setValue(numberOfLastRacesToShow);
        newSettings.activeRaceColumnSelectionStrategy.setValue(RaceColumnSelectionStrategies.LAST_N);
        return newSettings;
    }

}
