package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class SideToWhichMarkAtLegStartWasRoundedColumn extends DetailTypeColumn<NauticalSide, String> {
    private final StringMessages stringMessages;
    
    public SideToWhichMarkAtLegStartWasRoundedColumn(String title, LegDetailField<NauticalSide> field,
            String headerStyle, String columnStyle, StringMessages stringMessages,
            DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(DetailType.SIDE_TO_WHICH_MARK_AT_LEG_START_WAS_ROUNDED, field, new TextCell(), headerStyle, columnStyle, displayedLeaderboardRowsProvider);
        this.stringMessages = stringMessages;
    }

    @Override
    public String getValue(LeaderboardRowDTO row) {
        return getField().get(row) == null ? null : getField().get(row) == NauticalSide.PORT ? stringMessages.portSide() : stringMessages.starboardSide();
    }
}
