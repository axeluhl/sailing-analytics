package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class SideToWhichMarkAtLegStartWasRoundedColumn extends DetailTypeColumn<Tack, String> {
    public SideToWhichMarkAtLegStartWasRoundedColumn(String title, LegDetailField<Tack> field, String headerStyle, String columnStyle) {
        super(DetailType.SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED, field, new TextCell(), headerStyle, columnStyle);
    }

    @Override
    public String getValue(LeaderboardRowDTO row) {
        return getField().get(row) == null ? null : getField().get(row).name();
    }
}
