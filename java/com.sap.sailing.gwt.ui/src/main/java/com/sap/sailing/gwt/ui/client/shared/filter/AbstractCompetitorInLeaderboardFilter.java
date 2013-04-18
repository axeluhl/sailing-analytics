package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.filter.AbstractFilter;
import com.sap.sailing.domain.common.filter.Filter;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public abstract class AbstractCompetitorInLeaderboardFilter<ValueType> extends AbstractFilter<CompetitorDTO, ValueType>
        implements Filter<CompetitorDTO, ValueType> {
    private LeaderboardFetcher contextProvider;
    
    public AbstractCompetitorInLeaderboardFilter() {
    }

    public void setContextProvider(LeaderboardFetcher contextProvider) {
        this.contextProvider = contextProvider;
    }

    protected LeaderboardDTO getLeaderboard() {
        return contextProvider.getLeaderboard();
    }

    protected LeaderboardFetcher getContextProvider() {
        return contextProvider;
    }
}
