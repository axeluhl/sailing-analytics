package com.sap.sailing.gwt.ui.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;


public class RaceAverageSpeedColumn extends FormattedDoubleLegDetailColumn {
    public RaceAverageSpeedColumn(String title, String unit, LegDetailField<Double> field, int decimals,
            CellTable<LeaderboardRowDAO> leaderboardTable, String headerStyle, String columnStyle) {
        super(title, unit, field, decimals, leaderboardTable, headerStyle, columnStyle);
    }

    
}
