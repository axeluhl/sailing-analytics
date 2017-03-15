package com.sap.sailing.gwt.autoplay.client.place.sixtyinch;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardTableResources;

public interface LeaderboardTableResourcesSixty extends LeaderboardTableResources {
    interface LeaderboardTableStyleSixty extends LeaderboardTableStyle {
    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "LeaderboardTableSixty.css" })
    LeaderboardTableResources.LeaderboardTableStyle cellTableStyle();
}