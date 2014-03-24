package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class TrackingQualityMinMaxRenderer extends MinMaxRenderer {

    public TrackingQualityMinMaxRenderer(HasStringAndDoubleValue valueProvider, Comparator<LeaderboardRowDTO> comparator) {
        super(valueProvider, comparator);
    }
    
    /**
     * Renders the value of a {@link LeaderboardRowDTO}. Values up to 1.5 are considered good, meaning that there is no more than
     * 50% overshoot compared to the average sampling interval; values worse than 300% of the average sampling interval are considered
     * bad; anything in between is "OK."
     * 
     * @param title
     *            tool tip title to display; if <code>null</code>, no tool tip will be rendered
     */
    public void render(Context context, LeaderboardRowDTO row, String title, SafeHtmlBuilder sb) {
        Double ratio = getValueProvider().getDoubleValue(row);
        if (ratio != null) {
            String stringValue = getValueProvider().getStringValueToRender(row);
            stringValue = stringValue == null ? "" : stringValue;
            final String barStyle;
            if (ratio <= 1.5) {
                barStyle = "minMaxBackgroundBarGood";
            } else if (ratio <= 3) {
                barStyle = "minMaxBackgroundBar";
            } else {
                barStyle = "minMaxBackgroundBarBad";
            }
            sb.appendHtmlConstant(
                    "<div " + (title == null ? "" : "title=\"" + title + "\" ") + "class=\"" + barStyle + "\" "
                            + "style=\"background-size: 100% 25px; \">").appendEscaped(stringValue)
                    .appendHtmlConstant("</div>");
        }
    }

}
