package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.Collator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.impl.InvertibleComparatorAdapter;

public class CompetitorColumnBase<T> {
    private final LeaderboardFetcher leaderboardFetcher;
    protected final StringMessages stringMessages;
    private final CompetitorFetcher<T> competitorFetcher;
    
    public CompetitorColumnBase(LeaderboardFetcher leaderboardFetcher, StringMessages stringMessages, CompetitorFetcher<T> competitorFetcher) {
        super();
        this.leaderboardFetcher = leaderboardFetcher;
        this.stringMessages = stringMessages;
        this.competitorFetcher = competitorFetcher;
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher.getLeaderboard();
    }

    public AbstractSafeHtmlCell<T> getCell(final LeaderboardDTO leaderboard) {
        return new AbstractSafeHtmlCell<T>(new AbstractSafeHtmlRenderer<T>() {
            @Override
            public SafeHtml render(T row) {
                return new SafeHtmlBuilder().appendEscaped(leaderboard.getDisplayName(competitorFetcher.getCompetitor(row))).toSafeHtml();
            }
        }) {
            @Override
            protected void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml data, SafeHtmlBuilder sb) {
                sb.append(data);
            }
        };
    }

    public InvertibleComparator<T> getComparator() {
        return new InvertibleComparatorAdapter<T>() {
            @Override
            public int compare(T o1, T o2) {
                return competitorFetcher.getCompetitor(o1).getSailID() == null ? competitorFetcher.getCompetitor(o2).getSailID() == null ? 0 : -1
                        : competitorFetcher.getCompetitor(o2).getSailID() == null ? 1 : Collator.getInstance().compare(
                                competitorFetcher.getCompetitor(o1).getSailID(), competitorFetcher.getCompetitor(o2).getSailID());
            }
        };
    }
    
    public SafeHtmlHeader getHeader() {
        return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.name()),
                stringMessages.competitorColumnTooltip());
    }

    public void render(T t, String competitorColorBarStyle, SafeHtmlBuilder sb) {
        sb.appendHtmlConstant("<div " + competitorColorBarStyle + ">");
        sb.appendEscaped(getLeaderboard().getDisplayName(competitorFetcher.getCompetitor(t)));
        sb.appendHtmlConstant("</div>");
    }

}
