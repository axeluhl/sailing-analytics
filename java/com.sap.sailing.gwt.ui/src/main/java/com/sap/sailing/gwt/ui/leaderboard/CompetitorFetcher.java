package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;

public interface CompetitorFetcher<T> {
    CompetitorWithBoatDTO getCompetitor(T t);
}
