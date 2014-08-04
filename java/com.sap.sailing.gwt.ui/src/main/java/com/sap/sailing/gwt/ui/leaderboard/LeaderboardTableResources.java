package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.user.cellview.client.CellTable;

public interface LeaderboardTableResources extends CellTable.Resources {
    interface LeaderboardTableStyle extends CellTable.Style {
        /**
         * Applied to header cells of race columns
         */
        String cellTableRaceColumnHeader();

        /**
         * Applied to header cells of race columns
         */
        String cellTableLegColumnHeader();

        /**
         * Applied to header cells of race columns
         */
        String cellTableLegDetailColumnHeader();

        /**
         * Applied to detail columns
         */
        String cellTableLegDetailColumn();

        /**
         * Applied to race columns
         */
        String cellTableRaceColumn();
        
        /**
         * Applied to checkbox column
         */
        String cellTableCheckboxColumnCell();

        /**
         * Applied to leg columns
         */
        String cellTableLegColumn();

        /**
         * Applied to the totals columns
         */
        String cellTableTotalColumn();

    }

    @Override
    @Source({ CellTable.Style.DEFAULT_CSS, "LeaderboardTable.css" })
    LeaderboardTableResources.LeaderboardTableStyle cellTableStyle();
}