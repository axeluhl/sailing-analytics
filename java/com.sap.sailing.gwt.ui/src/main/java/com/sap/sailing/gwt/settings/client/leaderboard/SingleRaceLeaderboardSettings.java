package com.sap.sailing.gwt.settings.client.leaderboard;

import java.util.Collection;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sse.common.settings.generic.BooleanSetting;

public class SingleRaceLeaderboardSettings extends LeaderboardSettings {
    private static final long serialVersionUID = 2891220120957743158L;

    private BooleanSetting showRaceRankColumn;
    
    public SingleRaceLeaderboardSettings(boolean showCompetitorBoatInfoColumnDefault) {
        super(showCompetitorBoatInfoColumnDefault);
    }

    public SingleRaceLeaderboardSettings(Collection<DetailType> maneuverDetailsToShow,
            Collection<DetailType> legDetailsToShow, Collection<DetailType> raceDetailsToShow,
            Collection<DetailType> overallDetailsToShow, Long delayBetweenAutoAdvancesInMilliseconds,
            boolean showAddedScores, boolean showCompetitorShortNameColumn, boolean showCompetitorFullNameColumn,
            boolean showCompetitorBoatInfoColumn, boolean isCompetitorNationalityColumnVisible,
            boolean showRaceRankColumn) {
        super(maneuverDetailsToShow, legDetailsToShow, raceDetailsToShow, overallDetailsToShow,
                delayBetweenAutoAdvancesInMilliseconds, showAddedScores, showCompetitorShortNameColumn,
                showCompetitorFullNameColumn, showCompetitorBoatInfoColumn, isCompetitorNationalityColumnVisible);
        this.showRaceRankColumn.setValue(showRaceRankColumn);
    }
    
    public boolean isShowRaceRankColumn() {
        return showRaceRankColumn.getValue();
    }
    
    @Override
    protected void addChildSettings() {
        super.addChildSettings();
        showRaceRankColumn = new BooleanSetting("racerank", this, false);
    }
}
