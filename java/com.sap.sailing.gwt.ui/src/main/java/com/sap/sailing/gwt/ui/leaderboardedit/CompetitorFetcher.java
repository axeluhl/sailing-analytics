package com.sap.sailing.gwt.ui.leaderboardedit;

import com.sap.sailing.gwt.ui.shared.CompetitorDTO;

public interface CompetitorFetcher<T> {
    CompetitorDTO getCompetitor(T t);
}
