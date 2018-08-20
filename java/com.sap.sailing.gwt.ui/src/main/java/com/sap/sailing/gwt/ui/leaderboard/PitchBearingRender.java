package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;
import com.sap.sailing.domain.common.tracking.BravoFix;

public class PitchBearingRender extends MinMaxRenderer<LeaderboardRowDTO> {

    private double maxAbsDoubleValue = 0.5d;

    public PitchBearingRender(HasStringAndDoubleValue<LeaderboardRowDTO> valueProvider, Comparator<LeaderboardRowDTO> comparator) {
        super(valueProvider, comparator);
    }
    
    /**
     * Renders the value of a {@link LeaderboardRowDTO}. Values greater than
     * {@link BravoFix#MIN_FOILING_HEIGHT_THRESHOLD} are considered OK, values greater than that plus 0.2m are considered good,
     * values below {@link BravoFix#MIN_FOILING_HEIGHT_THRESHOLD} are considered bad.
     */
    @Override
    protected void render(LeaderboardRowDTO row, String nullSafeValue, String nullSafeTitle, SafeHtmlBuilder sb) {
        Double currentDoubleValue = getValueProvider().getDoubleValue(row);
        if (currentDoubleValue != null) {
            double absCurrentDoubleValue = Math.abs(currentDoubleValue);
            maxAbsDoubleValue = Math.max(absCurrentDoubleValue, maxAbsDoubleValue);
            double percent = (absCurrentDoubleValue / maxAbsDoubleValue * 100d / 2d);
            if (percent < 0.02) {
                sb.append(TEMPLATES.renderMiddle(nullSafeValue, BACKGROUND_BAR_STYLE_OK, nullSafeTitle));
            } else if (currentDoubleValue > 0) {
                sb.append(TEMPLATES.renderPositiveFromMiddle(nullSafeValue, BACKGROUND_BAR_STYLE_OK, nullSafeTitle,
                        percent));
            } else {
                sb.append(TEMPLATES.renderNegativeFromMiddle(nullSafeValue, BACKGROUND_BAR_STYLE_OK, nullSafeTitle,
                        percent));
            }
        }
    }
}
