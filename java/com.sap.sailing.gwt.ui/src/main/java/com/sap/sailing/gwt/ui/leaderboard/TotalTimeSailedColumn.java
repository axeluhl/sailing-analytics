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
public class TotalTimeSailedColumn extends FormattedDoubleLegDetailColumn {
    
    private static class TotalTimeSailedField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedInSeconds;
        }
    }

    public TotalTimeSailedColumn(StringMessages stringMessages, String headerStyle, String columnStyle) {
        super(stringMessages.totalTimeSailedInSeconds(), stringMessages.secondsUnit(),
                new TotalTimeSailedField(),
                DetailType.TOTAL_TIME_SAILED_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }

}
