package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * Renders the values and the percentage bar of the {@link DetailTypeColumn} and {@link ManeuverCountRaceColumn}.
 * It is used to update the minimum and maximum values of the columns, and to render the column content.
 * 
 * @author Fabian Schwarz-Fritz
 * 
 */
public class MinMaxRenderer {
    private final HasStringAndDoubleValue valueProvider;
    private final Comparator<LeaderboardRowDTO> comparator;
    private Double minimumValue;
    private Double maximumValue;

    /**
     * Renders the value and the percentage bar of the columns {@link DetailTypeColumn} and
     * {@link ManeuverCountRaceColumn}.
     * 
     * @param valueProvider
     *            Gets the String value of a {@link LeaderboardRowDTO}.
     * @param comparator
     *            The comparator to update the minimum and maximum values.
     */
    public MinMaxRenderer(HasStringAndDoubleValue valueProvider, Comparator<LeaderboardRowDTO> comparator) {
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    /**
     * Renders the value of a {@link LeaderboardRowDTO}.
     * 
     * @param title
     *            tool tip title to display; if <code>null</code>, no tool tip will be rendered
     */
    public void render(Context context, LeaderboardRowDTO row, String title, SafeHtmlBuilder sb) {
        int percent = getPercentage(row);
        String stringValue = valueProvider.getStringValueToRender(row);
        stringValue = stringValue == null ? "" : stringValue;
        sb.appendHtmlConstant("<div " + (title == null ? "" : "title=\"" + title + "\" ")
        		+ "class=\"minMaxBackgroundBar\" "
                + "style=\"background-size: " + percent + "% 25px; \">").appendEscaped(stringValue).appendHtmlConstant("</div>");
    }

    /**
     * Gets the percentage of a {@link LeaderboardRowDTO}. If no minimum or maximum value was set by calling
     * {@link MinMaxRenderer#updateMinMax(DisplayedLeaderboardRowsProvider)} before zero is returned.
     * 
     * @param row
     *            The row to get the percentage for.
     */
    protected int getPercentage(LeaderboardRowDTO row) {
        int percentage = 0;
        Double value = valueProvider.getDoubleValue(row);
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
     * Updates the {@link MinMaxRenderer#minimumValue} and {@link MinMaxRenderer#maximumValue}.
     * 
     * @param displayedLeaderboardRowsProvider
     *            The values of {@link LeaderboardRowDTO}s to determine the minimum and maximum values for.
     */
    public void updateMinMax(DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        LeaderboardRowDTO minimumRow = null;
        LeaderboardRowDTO maximumRow = null;
        for (LeaderboardRowDTO row : displayedLeaderboardRowsProvider.getRowsToDisplay()) {
            if (valueProvider.getDoubleValue(row) != null
                    && (minimumRow == null || comparator.compare(minimumRow, row) > 0)) {
                minimumRow = row;
            }
            if (valueProvider.getDoubleValue(row) != null
                    && (maximumRow == null || comparator.compare(maximumRow, row) < 0)) {
                maximumRow = row;
            }
        }
        if (minimumRow != null) {
            minimumValue = valueProvider.getDoubleValue(minimumRow);
        }
        if (maximumRow != null) {
            maximumValue = valueProvider.getDoubleValue(maximumRow);
        }
    }

    protected HasStringAndDoubleValue getValueProvider() {
        return valueProvider;
    }

}
