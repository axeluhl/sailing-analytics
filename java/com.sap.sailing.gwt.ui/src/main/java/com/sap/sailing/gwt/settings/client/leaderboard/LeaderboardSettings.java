package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.BooleanSetting;
import com.sap.sse.common.settings.generic.EnumSetSetting;
import com.sap.sse.common.settings.generic.LongSetting;

/**
 * Settings for the {@link LeaderboardPanel} component. If you change here, please also visit
 * {@link LeaderboardSettingsDialogComponent} to make the setting editable, and edit {@link LeaderboardUrlSettings}
 * for URL generation and parsing.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public abstract class LeaderboardSettings extends AbstractGenericSerializableSettings {
    private static final long serialVersionUID = 2625004077963291333L;
    
    protected EnumSetSetting<DetailType> maneuverDetailsToShow;
    protected EnumSetSetting<DetailType> legDetailsToShow;
    protected EnumSetSetting<DetailType> raceDetailsToShow;
    protected EnumSetSetting<DetailType> overallDetailsToShow;
    protected LongSetting delayBetweenAutoAdvancesInMilliseconds;
    protected BooleanSetting isShowCompetitorNationality;
    
    /**
     * Shows scores sum'd up for each race column
     */
    protected BooleanSetting showAddedScores;
    
    protected BooleanSetting showCompetitorShortNameColumn;
    protected BooleanSetting showCompetitorFullNameColumn;
    protected BooleanSetting showCompetitorBoatInfoColumn;
        
    @Override
    protected void addChildSettings() {
        isShowCompetitorNationality = new BooleanSetting("showCompetitorNationality", this, false);
        List<DetailType> maneuverDetails = new ArrayList<>();
        maneuverDetails.add(DetailType.TACK);
        maneuverDetails.add(DetailType.JIBE);
        maneuverDetails.add(DetailType.PENALTY_CIRCLE);
        maneuverDetailsToShow = new EnumSetSetting<>("maneuverDetailsToShow", this, maneuverDetails, DetailType::valueOfString);
        List<DetailType> legDetails = new ArrayList<>();
        legDetails.add(DetailType.LEG_DISTANCE_TRAVELED);
        legDetails.add(DetailType.LEG_AVERAGE_SPEED_OVER_GROUND_IN_KNOTS);
        legDetails.add(DetailType.LEG_RANK_GAIN);
        legDetailsToShow = new EnumSetSetting<>("legDetailsToShow", this, legDetails, DetailType::valueOfString);
        List<DetailType> raceDetails = new ArrayList<>();
        raceDetails.add(DetailType.RACE_DISPLAY_LEGS);
        raceDetailsToShow = new EnumSetSetting<>("raceDetailsToShow", this, raceDetails, DetailType::valueOfString);
        List<DetailType> overallDetails = new ArrayList<>();
        overallDetails.add(DetailType.REGATTA_RANK);
        overallDetailsToShow = new EnumSetSetting<>("overallDetailsToShow", this, overallDetails, DetailType::valueOfString);
        delayBetweenAutoAdvancesInMilliseconds = new LongSetting("delayBetweenAutoAdvancesInMilliseconds", this, LeaderboardEntryPoint.DEFAULT_REFRESH_INTERVAL_MILLIS);
        showAddedScores = new BooleanSetting("showAddedScores", this, false);
        showCompetitorShortNameColumn = new BooleanSetting("showCompetitorShortNameColumn", this, true);
        showCompetitorFullNameColumn = new BooleanSetting("showCompetitorFullNameColumn", this, true);
        showCompetitorBoatInfoColumn = new BooleanSetting("showCompetitorBoatInfoColumn", this, false);
    }
    
    public LeaderboardSettings(boolean showCompetitorBoatInfoColumnDefault) {
        showCompetitorBoatInfoColumn.setDefaultValue(showCompetitorBoatInfoColumnDefault);
    }
    
    /**
     * @param raceColumnsToShow <code>null</code> means don't modify the list of races shown
     */
    public LeaderboardSettings(Collection<DetailType> maneuverDetailsToShow, Collection<DetailType> legDetailsToShow,
            Collection<DetailType> raceDetailsToShow, Collection<DetailType> overallDetailsToShow,
            Long delayBetweenAutoAdvancesInMilliseconds, 
            boolean showAddedScores, boolean showCompetitorShortNameColumn, 
            boolean showCompetitorFullNameColumn, boolean showCompetitorBoatInfoColumn,
            boolean isCompetitorNationalityColumnVisible) {
        this.legDetailsToShow.setValues(legDetailsToShow);
        this.raceDetailsToShow.setValues(raceDetailsToShow);
        this.overallDetailsToShow.setValues(overallDetailsToShow);
        this.delayBetweenAutoAdvancesInMilliseconds.setValue(delayBetweenAutoAdvancesInMilliseconds);
        this.maneuverDetailsToShow.setValues(maneuverDetailsToShow);
        this.showAddedScores.setValue(showAddedScores);
        this.showCompetitorShortNameColumn.setValue(showCompetitorShortNameColumn);
        this.showCompetitorFullNameColumn.setValue(showCompetitorFullNameColumn);
        this.showCompetitorBoatInfoColumn.setValue(showCompetitorBoatInfoColumn);
        this.isShowCompetitorNationality.setValue(isCompetitorNationalityColumnVisible);
    }
  
    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public Collection<DetailType> getManeuverDetailsToShow() {
        return Util.createSet(maneuverDetailsToShow.getValues());
    }

    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public Collection<DetailType> getLegDetailsToShow() {
        return Util.createSet(legDetailsToShow.getValues());
    }

    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public Collection<DetailType> getRaceDetailsToShow() {
        return Util.createSet(raceDetailsToShow.getValues());
    }
    
    /**
     * A live collection that reflects the current state of the settings of a leaderboard panel
     */
    public Collection<DetailType> getOverallDetailsToShow() {
        return Util.createSet(overallDetailsToShow.getValues());
    }
    
    /**
     * @return if <code>null</code>, leave refresh interval alone (don't change in
     *         {@link LeaderboardPanel#updateSettings(LeaderboardSettings)}
     */
    public Long getDelayBetweenAutoAdvancesInMilliseconds() {
        return delayBetweenAutoAdvancesInMilliseconds.getValue();
    }

    public boolean isShowAddedScores() {
        return showAddedScores.getValue();
    }
    
    public boolean isShowCompetitorShortNameColumn() {
        return showCompetitorShortNameColumn.getValue();
    }
    
    public boolean isShowCompetitorFullNameColumn() {
        return showCompetitorFullNameColumn.getValue();
    }

    public boolean isShowCompetitorBoatInfoColumn() {
        return showCompetitorBoatInfoColumn.getValue();
    }

    public boolean isShowCompetitorNationality() {
        return isShowCompetitorNationality.getValue();
    }
}
