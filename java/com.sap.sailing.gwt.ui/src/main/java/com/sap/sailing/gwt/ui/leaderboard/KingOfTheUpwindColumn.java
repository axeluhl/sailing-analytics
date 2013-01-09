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
public class KingOfTheUpwindColumn extends AbstractTimeInHoursMinutesSecondsColumn {
    
    private static class KingOfTheUpwindField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedUpwindInSeconds;
        }
    }

    public KingOfTheUpwindColumn(StringMessages stringMessages, String headerStyle, String columnStyle) {
        super(stringMessages.totalTimeSailedUpwindInSeconds(), "[" + stringMessages.hhmmssUnit() + "]",
                new KingOfTheUpwindField(),
                DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_UPWIND_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }

}
