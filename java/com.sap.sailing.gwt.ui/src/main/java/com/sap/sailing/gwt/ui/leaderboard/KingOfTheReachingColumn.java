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
public class KingOfTheReachingColumn extends AbstractTimeInHoursMinutesSecondsColumn {
    
    private static class KingOfTheReachingField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedReachingInSeconds;
        }
    }

    public KingOfTheReachingColumn(StringMessages stringMessages, String headerStyle, String columnStyle) {
        super(stringMessages.totalTimeSailedReachingInSeconds(), "[" + stringMessages.hhmmssUnit() + "]",
                new KingOfTheReachingField(),
                DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_REACHING_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }
}
