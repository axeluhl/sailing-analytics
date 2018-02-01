package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * This interface helps to get the String or the Double values of a {@link LeaderboardRowDTO}.
 */
public interface HasStringAndDoubleValue {
    /**
     * This method returns the value of a column to render in MinMaxRenderer. If the value does not exist or is empty
     * for <code>null</code> is returned.
     * 
     * @return the value of the Column
     */
    String getStringValueToRender(LeaderboardRowDTO row);

    Double getDoubleValue(LeaderboardRowDTO row);
}
