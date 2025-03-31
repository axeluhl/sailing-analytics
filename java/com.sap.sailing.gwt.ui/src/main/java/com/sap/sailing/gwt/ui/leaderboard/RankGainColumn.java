package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.dto.LeaderboardRowDTO;

public class RankGainColumn extends DetailTypeColumn<Integer, Integer, LeaderboardRowDTO> {
    private static final LeaderboardResources leaderboardResources = GWT.create(LeaderboardResources.class);
	
    public RankGainColumn(String title, DataExtractor<Integer, LeaderboardRowDTO> field, String headerStyle, String columnStyle,
            DisplayedLeaderboardRowsProvider displayedLeaderboardRowsProvider) {
        super(DetailType.LEG_RANK_GAIN, field, new RankGainCell(), headerStyle, columnStyle, displayedLeaderboardRowsProvider);
    }

    @Override
    public Integer getValue(LeaderboardRowDTO row) {
        return getField().get(row);
    }

    private static class RankGainCell extends AbstractSafeHtmlCell<Integer> {
        public RankGainCell() {
            super(new SafeHtmlRenderer<Integer>() {
                @Override
                public SafeHtml render(Integer rank) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(rank, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(Integer rankDelta, SafeHtmlBuilder builder) {
                    if (rankDelta != null) {
                        builder.append(Math.abs(rankDelta));
                        builder.appendHtmlConstant("&nbsp;");
                        ImageResourceRenderer imgRenderer = new ImageResourceRenderer();
                        if (rankDelta < 0) {
                        	builder.append(imgRenderer.render(leaderboardResources.arrowGainIcon()));
                        } else if (rankDelta > 0) {
                        	builder.append(imgRenderer.render(leaderboardResources.arrowLossIcon()));
                        } else {
                        	builder.append(imgRenderer.render(leaderboardResources.arrowGainLossIcon()));
                        }
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
