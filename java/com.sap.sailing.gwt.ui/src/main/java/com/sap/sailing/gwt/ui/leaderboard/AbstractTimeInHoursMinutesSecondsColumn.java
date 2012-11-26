package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class AbstractTimeInHoursMinutesSecondsColumn extends FormattedDoubleLegDetailColumn {

    public AbstractTimeInHoursMinutesSecondsColumn(String title, String unit,
            com.sap.sailing.gwt.ui.leaderboard.LegDetailColumn.LegDetailField<Double> field, int decimals, SortingOrder preferredSortingOrder,
            String headerStyle, String columnStyle) {
        super(title, unit, field, decimals, preferredSortingOrder, headerStyle, columnStyle);
    }

    @Override
    public String getStringValueToRender(LeaderboardRowDTO object) {
        Double timeInSeconds = getDoubleValue(object);
        String result;
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
