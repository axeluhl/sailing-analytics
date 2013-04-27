package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.filter.FilterOperators;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public abstract class AbstractCompetitorInLeaderboardFilter<ValueType> extends AbstractFilterWithUI<CompetitorDTO, ValueType> {
    private LeaderboardFetcher contextProvider;
    private RaceIdentifier selectedRace;
    
    public AbstractCompetitorInLeaderboardFilter() {
        super(null);
    }

    public AbstractCompetitorInLeaderboardFilter(FilterOperators defaultOperator) {
        super(defaultOperator);
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

    protected RaceIdentifier getSelectedRace() {
        return selectedRace;
    }

    public void setSelectedRace(RaceIdentifier selectedRace) {
        this.selectedRace = selectedRace;
    }
}
