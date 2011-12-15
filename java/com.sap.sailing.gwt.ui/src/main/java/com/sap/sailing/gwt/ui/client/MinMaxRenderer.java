package com.sap.sailing.gwt.ui.client;

import java.util.Collection;
import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class MinMaxRenderer {
    private HasStringValue valueProvider;
    private Comparator<LeaderboardRowDAO> comparator;
    private Double minimumValue;
    private Double maximumValue;

    /**
     * Renders the value and the percentage bar of the columns {@link LegDetailColumn} and
     * {@link ManeuverCountRaceColumn}.
     * 
     * @param valueProvider
     *            Gets the String value of a {@link LeaderboardRowDAO}.
     * @param comparator
     *            The comparator to update the minimum and maximum values.
     */
    public MinMaxRenderer(HasStringValue valueProvider, Comparator<LeaderboardRowDAO> comparator) {
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    /**
     * Renders the value of a {@link LeaderboardRowDAO}.
     */
    public void render(Context context, LeaderboardRowDAO row, SafeHtmlBuilder sb) {
        int percent = getPercentage(row);
        String title = null;
        String stringValue = valueProvider.getStringValueToRender(row);
        stringValue = stringValue == null ? "" : stringValue;
        sb.appendHtmlConstant(
                "<div " + (title == null ? "" : "title=\"" + title + "\" ")
                        + "style=\"left: 0px; background-image: url(/images/greyBar.png); "
                        + " background-position: left; background-repeat: no-repeat; background-size: " + percent
                        + "% 25px; \">").appendEscaped(stringValue).appendHtmlConstant("</div>");
    }

    /**
     * Gets the percentage of a {@link LeaderboardRowDAO}. If no minimum or maximum value was set by calling
     * {@link MinMaxRenderer#updateMinMax(Collection)} before zero is returned.
     * 
     * @param row
     *            The row to get the percentage for.
     */
    private int getPercentage(LeaderboardRowDAO row) {
        int percentage = 0;
        Double value = getDoubleFromString(valueProvider.getStringValueToRender(row));
        if (value != null) {
            if (value != null && getMinimumDouble() != null && getMaximumDouble() != null) {
                int minBarLength = Math.abs(getMinimumDouble()) < 0.01 ? 0 : 10;
                percentage = (int) (minBarLength + (100. - minBarLength) * (value - getMinimumDouble())
                        / (getMaximumDouble() - getMinimumDouble()));
            }

        }
        return percentage;

    }

    private Double getMinimumDouble() {
        return minimumValue;
    }

    private Double getMaximumDouble() {
        return maximumValue;
    }

    /**
     * Parses a <code>String</code> into a <code>Double</code>.
     * 
     * @param string
     *            The <code>String</code> to be parsed.
     * @return The <code>Double</code> result of the given <code>String</code>.
     */
    private Double getDoubleFromString(String string) {
        Double result = null;
        if (string != null) {
            try {
                result = Double.parseDouble(string);
            } catch (NumberFormatException numberFormatException) {

            }
        }
        return result;
    }

    /**
     * Updates the {@link MinMaxRenderer#minimumValue} and {@link MinMaxRenderer#maximumValue}.
     * 
     * @param values
     *            The values of {@link LeaderboardRowDAO}s to determine the minimum and maximum values for.
     */
    public void updateMinMax(Collection<LeaderboardRowDAO> values) {
        LeaderboardRowDAO minimumRow = null;
        LeaderboardRowDAO maximumRow = null;
        for (LeaderboardRowDAO row : values) {
            if (getDoubleFromString(valueProvider.getStringValueToRender(row)) != null
                    && (minimumRow == null || comparator.compare(minimumRow, row) > 0)) {
                minimumRow = row;
            }
            if (getDoubleFromString(valueProvider.getStringValueToRender(row)) != null
                    && (maximumRow == null || comparator.compare(maximumRow, row) < 0)) {
                maximumRow = row;
            }
        }
        if (minimumRow != null) {
            minimumValue = getDoubleFromString(valueProvider.getStringValueToRender(minimumRow));
        }
        if (maximumRow != null) {
            maximumValue = getDoubleFromString(valueProvider.getStringValueToRender(maximumRow));
        }
    }
}
