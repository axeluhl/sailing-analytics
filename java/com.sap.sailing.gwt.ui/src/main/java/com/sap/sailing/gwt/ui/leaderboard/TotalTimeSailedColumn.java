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
public class TotalTimeSailedColumn extends FormattedDoubleLegDetailColumn {
    
    private static class TotalTimeSailedField implements LegDetailField<Double> {
        @Override
        public Double get(LeaderboardRowDTO row) {
            return row.totalTimeSailedInSeconds;
        }
    }

    public TotalTimeSailedColumn(StringMessages stringMessages, String headerStyle, String columnStyle) {
        super(stringMessages.totalTimeSailedInSeconds(), "[" + stringMessages.hhmmssUnit() + "]",
                new TotalTimeSailedField(),
                DetailType.TOTAL_TIME_SAILED_IN_SECONDS.getPrecision(),
                DetailType.TOTAL_TIME_SAILED_IN_SECONDS.getDefaultSortingOrder(),
                headerStyle, columnStyle);
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        String result;
        Double timeInSeconds = getDoubleValue(object);
        if (timeInSeconds == null) {
            result = null;
        } else {
            int hh = (int) (timeInSeconds / 3600);
            int mm = (int) ((timeInSeconds - 3600 * hh) / 60);
            int ss = (int) (timeInSeconds - 3600 * hh - 60 * mm);
            NumberFormat numberFormat = NumberFormatterFactory.getDecimalFormat(2, 0);
            result = "" + numberFormat.format(hh) + ":" + numberFormat.format(mm) + ":" + numberFormat.format(ss);
        }
        return result;
    }
}
