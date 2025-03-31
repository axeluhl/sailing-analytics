package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class TrackingQualityMinMaxRenderer extends MinMaxRenderer<LeaderboardRowDTO> {

    public TrackingQualityMinMaxRenderer(HasStringAndDoubleValue<LeaderboardRowDTO> valueProvider, Comparator<LeaderboardRowDTO> comparator) {
        super(valueProvider, comparator);
    }
    
    /**
     * Renders the value of a {@link LeaderboardRowDTO}. Values up to 1.5 are considered good, meaning that there is no more than
     * 50% overshoot compared to the average sampling interval; values worse than 300% of the average sampling interval are considered
     * bad; anything in between is "OK."
     */
    @Override
    protected void render(LeaderboardRowDTO row, String nullSafeValue, String nullSafeTitle, SafeHtmlBuilder sb) {
        Double ratio = getValueProvider().getDoubleValue(row);
        if (ratio != null) {
            final String barStyle;
            if (ratio <= 1.5) barStyle = BACKGROUND_BAR_STYLE_GOOD;
            else if (ratio <= 3) barStyle = BACKGROUND_BAR_STYLE_OK;
            else barStyle = BACKGROUND_BAR_STYLE_BAD;
            sb.append(TEMPLATES.render(nullSafeValue, barStyle, nullSafeTitle, 100));
        }
    }

}
