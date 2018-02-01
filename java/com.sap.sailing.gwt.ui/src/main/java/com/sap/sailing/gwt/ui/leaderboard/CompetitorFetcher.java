package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.CompetitorDTO;

public interface CompetitorFetcher<T> {
    CompetitorDTO getCompetitor(T t);
}
