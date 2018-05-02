package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.filter.AbstractNumberFilter;

/**
 * A filter filtering competitors by their total rank
 * @author Frank
 *
 */
public class CompetitorTotalRankFilter extends AbstractNumberFilter<CompetitorDTO, Integer> implements
        LeaderboardFilterContext, FilterWithUI<CompetitorDTO> {
    public static final String FILTER_NAME = "CompetitorTotalRankFilter";

    private LeaderboardFetcher leaderboardFetcher;

    public CompetitorTotalRankFilter() {
    }

    private LeaderboardDTO getLeaderboard() {
        return leaderboardFetcher != null ? leaderboardFetcher.getLeaderboard() : null;
    }

    @Override
    public boolean matches(CompetitorDTO competitorDTO) {
        boolean result = false;
        if (value > 0 && operator != null && getLeaderboard() != null) {
            int totalRank = getLeaderboard().getTotalRank(competitorDTO);
            result = operator.matchValues(value, totalRank);
        }
        return result;
    }
        
    @Override
    public String getName() {
        return FILTER_NAME;
    }

    @Override
    public String getLocalizedName(StringMessages stringMessages) {
        return stringMessages.totalRegattaRank();
    }

    @Override
    public String getLocalizedDescription(StringMessages stringMessages) {
        return "Top "  + this.getValue() + " " + stringMessages.totalRegattaRank();
    }

    @Override
    public String validate(StringMessages stringMessages) {
        String errorMessage = null;
        if(value != null) {
            Integer intfilterValue = (Integer) value;
            if(intfilterValue <= 0) {
                errorMessage = stringMessages.numberMustBePositive();
            }
        } else {
            errorMessage = stringMessages.pleaseEnterANumber();
        }
        return errorMessage;
    }

    @Override
    public CompetitorTotalRankFilter copy() {
        CompetitorTotalRankFilter result = new CompetitorTotalRankFilter();
        result.setValue(getValue());
        result.setOperator(getOperator());
        return result;
    }

    @Override
    public LeaderboardFetcher getLeaderboardFetcher() {
        return leaderboardFetcher;
    }

    @Override
    public void setLeaderboardFetcher(LeaderboardFetcher leaderboardFetcher) {
        this.leaderboardFetcher = leaderboardFetcher;
    }

    @Override
    public FilterUIFactory<CompetitorDTO> createUIFactory() {
        return new CompetitorTotalRankFilterUIFactory(this);
    }
}
