package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public interface CompetitorFetcher<T> {
    CompetitorDTO getCompetitor(T t);
}
