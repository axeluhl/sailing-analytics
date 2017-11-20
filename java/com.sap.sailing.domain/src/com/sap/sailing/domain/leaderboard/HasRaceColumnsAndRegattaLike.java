package com.sap.sailing.domain.leaderboard;

import com.sap.sailing.domain.leaderboard.impl.CompetitorProviderFromRaceColumnsAndRegattaLike;
import com.sap.sailing.domain.regattalike.HasRegattaLike;

public interface HasRaceColumnsAndRegattaLike extends HasRegattaLike, HasRaceColumns {

    CompetitorProviderFromRaceColumnsAndRegattaLike getOrCreateCompetitorsProvider();
}
