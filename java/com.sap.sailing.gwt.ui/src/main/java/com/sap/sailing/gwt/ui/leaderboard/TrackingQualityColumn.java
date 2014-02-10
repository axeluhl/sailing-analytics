package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class TrackingQualityColumn extends DetailTypeColumn<Double, Double> {
    public TrackingQualityColumn(String title, LegDetailField<Double> field, String headerStyle, String columnStyle) {
        super(DetailType.RACE_TRACKING_QUALITY, field, new TrackingQualityCell(), headerStyle, columnStyle);
    }

    @Override
    public Double getValue(LeaderboardRowDTO row) {
        return getField().get(row);
    }

    private static class TrackingQualityCell extends AbstractSafeHtmlCell<Double> {
        private static final String STYLE_TRACKING_QUALITY_CIRCLE = "trackingQuality-circle";
        private static final String STYLE_TRACKING_QUALITY_CIRCLE_RED = "circleRed";
        private static final String STYLE_TRACKING_QUALITY_CIRCLE_YELLOW = "circleYellow";
        private static final String STYLE_TRACKING_QUALITY_CIRCLE_GREEN = "circleGreen";

        public TrackingQualityCell() {
            super(new SafeHtmlRenderer<Double>() {
                @Override
                public SafeHtml render(Double rank) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(rank, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(Double ratioOfLagAndAverageSamplingInterval, SafeHtmlBuilder builder) {
                    if (ratioOfLagAndAverageSamplingInterval != null) {
                        builder.append(SafeHtmlUtils.fromTrustedString("<div class=\"" + STYLE_TRACKING_QUALITY_CIRCLE + " "));
                        if (ratioOfLagAndAverageSamplingInterval < 3.0) {
                            builder.append(SafeHtmlUtils.fromTrustedString(STYLE_TRACKING_QUALITY_CIRCLE_GREEN));
                        } else if (ratioOfLagAndAverageSamplingInterval < 5) {
                            builder.append(SafeHtmlUtils.fromTrustedString(STYLE_TRACKING_QUALITY_CIRCLE_YELLOW));
                        } else {
                            builder.append(SafeHtmlUtils.fromTrustedString(STYLE_TRACKING_QUALITY_CIRCLE_RED));
                        }
                        builder.append(SafeHtmlUtils.fromTrustedString("\"></div>"));
                    } else {
                        builder.append(SafeHtmlUtils.fromTrustedString(STYLE_TRACKING_QUALITY_CIRCLE_GREEN));
                    }
                }
            });
        }

        @Override
        protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
            if (data != null) {
                sb.append(data);
            }
        }
    }
}
