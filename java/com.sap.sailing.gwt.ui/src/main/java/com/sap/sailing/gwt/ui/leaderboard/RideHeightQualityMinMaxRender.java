package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class RideHeightQualityMinMaxRender extends MinMaxRenderer {

    public RideHeightQualityMinMaxRender(HasStringAndDoubleValue valueProvider, Comparator<LeaderboardRowDTO> comparator) {
        super(valueProvider, comparator);
    }
    
    /**
     * Renders the value of a {@link LeaderboardRowDTO}. Values greater than 1.26 are considered good,
     * values lower than 0.74 are considered bad; anything in between is "OK".
     */
    @Override
    protected void render(LeaderboardRowDTO row, String nullSafeValue, String nullSafeTitle, SafeHtmlBuilder sb) {
        Double doubleValue = getValueProvider().getDoubleValue(row);
        if (doubleValue != null) {
            final String barStyle;
            if (doubleValue > 1.26) barStyle = BACKGROUND_BAR_STYLE_GOOD;
            else if (doubleValue >= 0.74) barStyle = BACKGROUND_BAR_STYLE_OK;
            else barStyle = BACKGROUND_BAR_STYLE_BAD;
            sb.append(TEMPLATES.render(nullSafeValue, barStyle, nullSafeTitle, getPercentage(row)));
        }
    }

}
