package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
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
        super(stringMessages.totalTimeSailedDownwindInSeconds(), "[" + stringMessages.hhmmssUnit() + "]",
                new KingOfTheDownwindField(),
                DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_DOWNWIND_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        Double timeInSeconds = getDoubleValue(object);
        int hh = (int) (timeInSeconds/3600);
        int mm = (int) ((timeInSeconds - 3600*hh)/60);
        int ss = (int) (timeInSeconds - 3600*hh - 60*mm);
        NumberFormat numberFormat = NumberFormatterFactory.getDecimalFormat(2, 0);
        return ""+numberFormat.format(hh)+":"+numberFormat.format(mm)+":"+numberFormat.format(ss);
    }

}
