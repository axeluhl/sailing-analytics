package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class FormattedDoubleLegDetailColumn extends LegDetailColumn<Double, String> {
    private final NumberFormat formatter;
    
    public FormattedDoubleLegDetailColumn(String title,
            com.sap.sailing.gwt.ui.client.LegDetailColumn.LegDetailField<Double> field,
            int decimals) {
        super(title, field, new TextCell());
        StringBuilder patternBuilder = new StringBuilder("0");
        if (decimals > 0) {
            patternBuilder.append('.');
        }
        for (int i=0; i<decimals; i++) {
            patternBuilder.append('0');
        }
        formatter = NumberFormat.getFormat(patternBuilder.toString());
    }

    @Override
    public String getValue(LeaderboardRowDAO row) {
        Double fieldValue = getField().get(row);
        String result = "";
        if (fieldValue != null) {
            result = formatter.format(fieldValue);
        }
        return result;
    }

}
