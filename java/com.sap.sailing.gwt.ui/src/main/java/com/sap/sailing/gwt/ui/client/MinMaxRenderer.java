package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class MinMaxRenderer {

    private LeaderboardDAO leaderboard;
    private HasStringValue valueProvider;
    private Comparator<LeaderboardRowDAO> comparator;
    private LeaderboardRowDAO minimumvalue;
    private LeaderboardRowDAO maximumValue;

    public MinMaxRenderer(LeaderboardDAO leaderboard, HasStringValue valueProvider,
            Comparator<LeaderboardRowDAO> comparator) {
        this.leaderboard = leaderboard;
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        int percent = getPercentage(row);
        String title = null;
        sb.appendHtmlConstant(
                "<div " + (title == null ? "" : "title=\"" + title + "\" ")
                        + "style=\"left: 0px; background-image: url(/images/greyBar.png); "
                        + " background-position: left; background-repeat: no-repeat; background-size: " + percent
                        + "% 25px; \">").appendEscaped(valueProvider.getStringValueToRender(row).toString())
                .appendHtmlConstant("</div>");
    }

    private int getPercentage(LeaderboardRowDAO row) {
        updateMinMax(leaderboard);
        String valueString = valueProvider.getStringValueToRender(row);
        int percentage = 0;
        if (valueString != null) {
            Double value = Double.parseDouble(valueString);
            if (value != null && getMinimumDouble() != null && getMaximumDouble() != null) {
                int minBarLength = Math.abs(getMinimumDouble()) < 0.01 ? 0 : 10;
                percentage = (int) (minBarLength + (100. - minBarLength) * (value - getMinimumDouble())
                        / (getMaximumDouble() - getMinimumDouble()));
            }
        }
        return percentage;

    }

    private Double getMinimumDouble() {
        String valueString = valueProvider.getStringValueToRender(minimumvalue);
        Double result = null;
        if (valueString != null || valueString != "") {
            result = Double.parseDouble(valueString);
        }
        return result;
    }

    private Double getMaximumDouble() {
        String valueString = valueProvider.getStringValueToRender(maximumValue);
        Double result = null;
        if (valueString != null || valueString != "") {
            result = Double.parseDouble(valueString);
        }
        return result;
    }

    public void updateMinMax(LeaderboardDAO leaderboard) {
        Collection<LeaderboardRowDAO> values = leaderboard.rows.values();
        LeaderboardRowDAO minimumRow = null;
        LeaderboardRowDAO maximumRow = null;
        for (LeaderboardRowDAO row : values) {
            if ((row) != null && (minimumRow == null || comparator.compare(minimumRow, row) > 0)) {
                minimumRow = row;
            }
            if (valueProvider.getStringValueToRender(row) != null
                    && (maximumRow == null || comparator.compare(maximumRow, row) < 0)) {
                maximumRow = row;
            }
        }
        if (minimumRow != null) {
            minimumvalue = minimumRow;
        }
        if (maximumRow != null) {
            maximumValue = maximumRow;
        }
    }
}
