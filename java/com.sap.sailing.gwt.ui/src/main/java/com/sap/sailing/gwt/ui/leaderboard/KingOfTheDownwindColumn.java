package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

/**
 * Shows the {@link LeaderboardRowDTO#totalTimeSailedDownwindInSeconds} attribute.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class KingOfTheDownwindColumn extends FormattedDoubleLegDetailColumn {
    
    private static class KingOfTheDownwindField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedDownwindInSeconds;
        }
    }

    public KingOfTheDownwindColumn(StringMessages stringMessages, String headerStyle, String columnStyle) {
        super(stringMessages.totalTimeSailedDownwindInSeconds(), stringMessages.secondsUnit(),
                new KingOfTheDownwindField(),
                DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }

}
