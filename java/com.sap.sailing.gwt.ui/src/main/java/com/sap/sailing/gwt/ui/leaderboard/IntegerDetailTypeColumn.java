package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Objects;

import com.google.gwt.cell.client.TextCell;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * Displays an {@code Integer} value as plain {@link String#valueOf(Object) string}.
 */
public class IntegerDetailTypeColumn extends DetailTypeColumn<Integer, String, LeaderboardRowDTO> {

    protected IntegerDetailTypeColumn(DetailType detailType, DataExtractor<Integer, LeaderboardRowDTO> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, new TextCell(), headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    public String getValue(LeaderboardRowDTO object) {
        final Integer value = getFieldValue(object);
        return Objects.isNull(value) ? null : String.valueOf(value);
    }
}
