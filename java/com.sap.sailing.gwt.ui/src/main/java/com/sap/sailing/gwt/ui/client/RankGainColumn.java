package com.sap.sailing.gwt.ui.client;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.sap.sailing.gwt.ui.shared.LeaderboardRowDAO;

public class RankGainColumn extends LegDetailColumn<Integer, Integer> {
    public RankGainColumn(String title, LegDetailField<Integer> field) {
        super(title, field, new RankGainCell());
    }

    @Override
    public Integer getValue(LeaderboardRowDAO row) {
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
                        if (rankDelta < 0) {
                            builder.appendHtmlConstant("<img src=\"/images/arrow-gain.png\"/>");
                        } else if (rankDelta > 0) {
                            builder.appendHtmlConstant("<img src=\"/images/arrow-loss.png\"/>");
                        }
                        builder.append(Math.abs(rankDelta));
                    }
                }
            });
        }

        @Override
        protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
            sb.append(data);
        }
    }

}
