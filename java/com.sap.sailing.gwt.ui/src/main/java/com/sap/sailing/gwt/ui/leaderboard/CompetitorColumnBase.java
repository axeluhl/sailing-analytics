package com.sap.sailing.gwt.ui.leaderboard;

import java.util.Comparator;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.filter.LeaderboardFetcher;
import com.sap.sse.common.util.NaturalComparator;

public class CompetitorColumnBase<T> {

    private final LeaderboardFetcher leaderboardFetcher;
    private final StringMessages stringMessages;
    private final CompetitorFetcher<T> competitorFetcher;
    
    public CompetitorColumnBase(LeaderboardFetcher leaderboardFetcher, StringMessages stringMessages,
            CompetitorFetcher<T> competitorFetcher) {
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
                return SafeHtmlUtils.fromString(leaderboard.getDisplayName(competitorFetcher.getCompetitor(row)));
            }
        }) {
            @Override
            protected void render(Context context, SafeHtml data, SafeHtmlBuilder sb) {
                sb.append(data);
            }
        };
    }

    public InvertibleComparator<T> getComparator() {
        return new InvertibleComparatorAdapter<T>() {
            @Override
            public int compare(T o1, T o2) {
                return Comparator
                        .<CompetitorDTO> nullsLast((c1, c2) -> new NaturalComparator(/* caseSensitive */ false)
                                .compare(c1.getShortName(), c2.getShortName()))
                        .compare(competitorFetcher.getCompetitor(o1), competitorFetcher.getCompetitor(o2));
            }
        };
    }
    
    public SafeHtmlHeader getHeader() {
        return new SafeHtmlHeaderWithTooltip(SafeHtmlUtils.fromString(stringMessages.name()),
                stringMessages.competitorColumnTooltip());
    }

    public void render(Context context, T row, SafeHtmlBuilder sb) {
        sb.appendEscaped(getLeaderboard().getDisplayName(competitorFetcher.getCompetitor(row)));
    }

}
