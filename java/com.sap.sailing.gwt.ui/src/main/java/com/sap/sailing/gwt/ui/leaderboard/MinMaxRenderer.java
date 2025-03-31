package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

/**
 * Renders the values and the percentage bar of the {@link DetailTypeColumn} and {@link ManeuverCountRaceColumn}.
 * It is used to update the minimum and maximum values of the columns, and to render the column content.
 * 
 * @author Fabian Schwarz-Fritz
 * 
 */
public class MinMaxRenderer<T> {
    protected static final String BACKGROUND_BAR_STYLE_BAD = "minMaxBackgroundBarBad";
    protected static final String BACKGROUND_BAR_STYLE_OK = "minMaxBackgroundBar";
    protected static final String BACKGROUND_BAR_STYLE_GOOD = "minMaxBackgroundBarGood";

    private final HasStringAndDoubleValue<T> valueProvider;
    /** used to determine minimum and maximum values for the rendered bars.*/
    private final Comparator<T> comparator;
    private Double minimumValue;
    private Double maximumValue;

    public static final Templates TEMPLATES = GWT.create(Templates.class);

    public static class Templates {
        protected interface MyTemplate extends SafeHtmlTemplates {
            @Template("<div title=\"{2}\" class=\"{1}\" style='{3}'>{0}</div>")
            SafeHtml render(String value, String cssClass, String title, SafeStyles style);

            @Template("<div title=\"{2}\" style='position:relative;'>"
                    + "<div class=\"{1}\" style=\"position:absolute;left:50%;width:1px;height:25px;background-repeat:repeat-x;\" ></div>"
                    + "<div style='position:relative;'>{0}</div>" //
                    + "</div>")
            SafeHtml renderMiddle(String value, String cssClass, String title);

            @Template("<div title=\"{2}\" style='position:relative;'>"
                    + "<div class=\"{1}\" style=\"{3}\" >&nbsp;</div>"
                    + "<div style='position:relative;'>{0}</div>" //
                    + "</div>")
            SafeHtml renderPositiveFromMiddle(String value, String cssClass, String title, SafeStyles style);

            @Template("<div title=\"{2}\" style='position:relative;'>"
                    + "<div class=\"{1}\" style=\"{3}\" >&nbsp;</div>"
                    + "<div style='position:relative;'>{0}</div>" //
                    + "</div>")
            SafeHtml renderNegativeFromMiddle(String value, String cssClass, String title, SafeStyles style);
        }
        protected static final MyTemplate T = GWT.create(MyTemplate.class);
        SafeHtml render(String value, String cssClass, String title, int percentage) {
            SafeStylesBuilder sb = new SafeStylesBuilder();
            sb.trustedNameAndValue("background-size", percentage + "% 25px");
            return T.render(value, cssClass, title, sb.toSafeStyles());
        }
        SafeHtml renderMiddle(String value, String cssClass, String title) {
            return T.renderMiddle(value, cssClass, title);
        }
        SafeHtml renderPositiveFromMiddle(String value, String cssClass, String title, double percentage) {
            SafeStylesBuilder sb = new SafeStylesBuilder();
            sb.width(percentage, Unit.PCT);
            sb.position(Position.ABSOLUTE);
            sb.left(50, Unit.PCT);
            sb.trustedNameAndValue("background-repeat", "repeat-x");
            return T.renderPositiveFromMiddle(value, cssClass, title, sb.toSafeStyles());
        }
        SafeHtml renderNegativeFromMiddle(String value, String cssClass, String title, double percentage) {
            SafeStylesBuilder sb = new SafeStylesBuilder();
            sb.width(percentage, Unit.PCT);
            sb.position(Position.ABSOLUTE);
            sb.right(50, Unit.PCT);
            sb.trustedNameAndValue("left", "initial");
            sb.trustedNameAndValue("background-repeat", "repeat-x");
            return T.renderNegativeFromMiddle(value, cssClass, title, sb.toSafeStyles());
        }
    }

    /**
     * Renders the value and the percentage bar of the columns {@link DetailTypeColumn} and
     * {@link ManeuverCountRaceColumn}.
     * 
     * @param valueProvider
     *            Gets the String value of a {@link LeaderboardRowDTO}.
     * @param comparator
     *            The comparator to update the minimum and maximum values.
     */
    public MinMaxRenderer(HasStringAndDoubleValue<T> valueProvider, Comparator<T> comparator) {
        this.valueProvider = valueProvider;
        this.comparator = comparator;
    }

    /**
     * Renders the value of a {@link LeaderboardRowDTO}.
     * 
     * @param title
     *            tool tip title to display; if <code>null</code>, no tool tip will be rendered
     */
    public final void render(Context context, T row, String title, SafeHtmlBuilder sb) {
        String stringValue = valueProvider.getStringValueToRender(row);
        this.render(row, stringValue == null ? "" : stringValue, title == null ? "" : title, sb);
    }
    
    protected void render(T row, String nullSafeValue, String nullSafeTitle, SafeHtmlBuilder sb) {
        sb.append(TEMPLATES.render(nullSafeValue, BACKGROUND_BAR_STYLE_OK, nullSafeTitle, getPercentage(row)));
    }
    
    /**
     * Gets the percentage of a {@link LeaderboardRowDTO}. If no minimum or maximum value was set by calling
     * {@link MinMaxRenderer#updateMinMax(DisplayedLeaderboardRowsProvider)} before zero is returned.
     * 
     * @param row
     *            The row to get the percentage for.
     */

    protected int getPercentage(T row) {        
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
    public void updateMinMax(Iterable<T> displayedLeaderboardRowsProvider) {
        T minimumOrderRow = null;
        T maximumOrderRow = null; 
        for (T row : displayedLeaderboardRowsProvider) {
            if (valueProvider.getDoubleValue(row) != null
                    && (minimumOrderRow == null || comparator.compare(minimumOrderRow, row) > 0)) {
                minimumOrderRow = row;
            }
            if (valueProvider.getDoubleValue(row) != null
                    && (maximumOrderRow == null || comparator.compare(maximumOrderRow, row) < 0)) {
                maximumOrderRow = row;
            }
        }        
        if (minimumOrderRow != null) {
            minimumValue = valueProvider.getDoubleValue(minimumOrderRow);

        }
        if (maximumOrderRow != null) {
            maximumValue = valueProvider.getDoubleValue(maximumOrderRow);
        }
    }

    protected HasStringAndDoubleValue<T> getValueProvider() {
        return valueProvider;
    }
}
