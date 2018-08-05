package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.EnumSetting;
import com.sap.sse.common.settings.generic.IntegerSetting;
import com.sap.sse.common.settings.generic.StringSetSetting;

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
        super();
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
       this.namesOfRaceColumnsToShow.setValues(namesOfRaceColumnsToShow);
    }
    
    public MultiRaceLeaderboardSettings overrideDefaultsForNamesOfRaceColumns(List<String> namesOfRaceColumns) {
        MultiRaceLeaderboardSettings newSettings = new MultiRaceLeaderboardSettings();
        newSettings.legDetailsToShow.setValues(this.getLegDetailsToShow());
        newSettings.raceDetailsToShow.setValues(this.getRaceDetailsToShow());
        newSettings.overallDetailsToShow.setValues(this.getOverallDetailsToShow());
        newSettings.numberOfLastRacesToShow.setValue(this.getNumberOfLastRacesToShow());
        newSettings.activeRaceColumnSelectionStrategy.setValue(this.getActiveRaceColumnSelectionStrategy());
        newSettings.delayBetweenAutoAdvancesInMilliseconds.setValue(this.getDelayBetweenAutoAdvancesInMilliseconds());
        newSettings.maneuverDetailsToShow.setValues(this.getManeuverDetailsToShow());
        newSettings.showAddedScores.setValue(this.isShowAddedScores());
        newSettings.showCompetitorShortNameColumn.setValue(this.isShowCompetitorShortNameColumn());
        newSettings.showCompetitorFullNameColumn.setValue(this.isShowCompetitorFullNameColumn());
        newSettings.showCompetitorBoatInfoColumn.setValue(this.isShowCompetitorBoatInfoColumn());
        newSettings.isShowCompetitorNationality.setValue(this.isShowCompetitorNationality());
        newSettings.namesOfRaceColumnsToShow.setValues(this.getNamesOfRaceColumnsToShow());
        newSettings.namesOfRaceColumnsToShow.setDefaultValues(namesOfRaceColumns);
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

}
