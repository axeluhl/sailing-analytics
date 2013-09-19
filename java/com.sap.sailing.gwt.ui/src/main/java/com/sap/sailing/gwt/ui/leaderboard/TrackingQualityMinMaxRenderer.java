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
     * Renders the value of a {@link LeaderboardRowDTO}.
     * 
     * @param title
     *            tool tip title to display; if <code>null</code>, no tool tip will be rendered
     */
    public void render(Context context, LeaderboardRowDTO row, String title, SafeHtmlBuilder sb) {
        int percent = getPercentage(row);
        String stringValue = getValueProvider().getStringValueToRender(row);
        stringValue = stringValue == null ? "" : stringValue;
        final String barStyle;
        if (percent < 33) {
            barStyle = "minMaxBackgroundBarGood";
        } else if (percent < 66) {
            barStyle = "minMaxBackgroundBar";
        } else {
            barStyle = "minMaxBackgroundBarBad";
        }
        sb.appendHtmlConstant("<div " + (title == null ? "" : "title=\"" + title + "\" ")
                        + "class=\""+barStyle+"\" "
                + "style=\"background-size: 100% 25px; \">").appendEscaped(stringValue).appendHtmlConstant("</div>");
    }

}
