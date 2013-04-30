package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.ValueFilterWithUI;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDTO;

public interface CompetitorInLeaderboardFilter<ValueType> extends ValueFilterWithUI<CompetitorDTO, ValueType> {
    public void setContextProvider(LeaderboardFetcher contextProvider);

    public LeaderboardDTO getLeaderboard();

    public LeaderboardFetcher getContextProvider();

    public RaceIdentifier getSelectedRace();

    public void setSelectedRace(RaceIdentifier selectedRace);
}
