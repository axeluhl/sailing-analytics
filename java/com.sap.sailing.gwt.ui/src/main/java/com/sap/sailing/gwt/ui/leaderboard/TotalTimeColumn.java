package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDTO;

public class TotalTimeColumn extends FormattedDoubleDetailTypeColumn {

    protected TotalTimeColumn(DetailType detailType, LegDetailField<Double> field, String headerStyle, String columnStyle) {
        super(detailType, field, headerStyle, columnStyle);
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
