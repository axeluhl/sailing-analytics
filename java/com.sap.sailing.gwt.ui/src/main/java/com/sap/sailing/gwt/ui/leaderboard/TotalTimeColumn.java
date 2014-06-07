package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;

public class TotalTimeColumn extends FormattedDoubleDetailTypeColumn {

    protected TotalTimeColumn(DetailType detailType, LegDetailField<Double> field, String headerStyle,
            String columnStyle, DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(detailType, field, headerStyle, columnStyle, displayedLeaderboardRowsProvider);
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
