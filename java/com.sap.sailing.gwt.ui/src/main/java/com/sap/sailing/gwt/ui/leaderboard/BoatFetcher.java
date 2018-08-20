package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.domain.common.dto.BoatDTO;

public interface BoatFetcher<T> {
    BoatDTO getBoat(T t);
}
